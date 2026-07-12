package com.aicolorpredict.analytics.domain.usecase

import android.util.Log
import com.aicolorpredict.analytics.ai.base.ModelRegistry
import com.aicolorpredict.analytics.ai.ensemble.AdaptiveWeightingModel
import com.aicolorpredict.analytics.ai.ensemble.EnsembleModel
import com.aicolorpredict.analytics.ai.learning.AdaptiveWeightingEngine
import com.aicolorpredict.analytics.ai.learning.IncrementalStatsCache
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.model.Prediction
import com.aicolorpredict.analytics.feature.FeatureEngineer
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Core prediction use case — performance-optimized.
 *
 * Key optimizations:
 *   1. **No DB query for history** — uses [IncrementalStatsCache.recentWindow]
 *      (O(1) memory access) instead of `roundRepo.lastN(1000)` (O(n) DB query).
 *   2. **Parallel model execution** — all 15 models run concurrently on
 *      [AppDispatchers.default] via `async`/`awaitAll`.
 *   3. **One-time cache init** — the stats cache is rebuilt from DB only on
 *      cold start; every subsequent prediction is O(1) for stats + O(k) for
 *      model inference.
 *   4. **Timing logs** — every phase is timed so bottlenecks are visible.
 */
class PredictUseCase @Inject constructor(
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val registry: ModelRegistry,
    private val featureEngineer: FeatureEngineer,
    private val ensemble: EnsembleModel,
    private val adaptive: AdaptiveWeightingModel,
    private val weightingEngine: AdaptiveWeightingEngine,
    private val statsCache: IncrementalStatsCache,
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke(): Prediction? = withContext(dispatchers.default) {
        val t0 = System.nanoTime()

        // One-time cache init (O(n) on cold start only)
        if (!statsCache.isInitialized()) {
            Log.d("Predict", "Cold start — rebuilding stats cache from DB...")
            val history = roundRepo.lastN(1000000).map { it.number }
            statsCache.rebuildFrom(history)
            Log.d("Predict", "Cache rebuilt: ${history.size} rounds in ${System.nanoTime() - t0}ns")
        }

        // O(1) history access from cache — no DB query
        val history = statsCache.recentWindow(1000)
        if (history.isEmpty()) return@withContext null

        // Get anchor round ID from DB (single-row query)
        val anchorRoundId = roundRepo.lastN(1).firstOrNull()?.id ?: return@withContext null
        val t1 = System.nanoTime()

        // Build features
        val features = featureEngineer.build(roundId = anchorRoundId, history = history)
        val t2 = System.nanoTime()

        // Pull current adaptive weights (O(1) — in-memory)
        val weights = weightingEngine.computeWeights(registry.names)
        val rollingAccuracies = registry.names.associateWith { name ->
            weightingEngine.getRollingTop1(name)
        }
        val t3 = System.nanoTime()

        // Run every model in parallel
        val outputs = coroutineScope {
            registry.models.map { model ->
                async(dispatchers.default) {
                    val ra = rollingAccuracies[model.name] ?: 0.1
                    model.predict(features, history, ra)
                }
            }.map { it.await() }
        }
        val t4 = System.nanoTime()

        // Persist outputs
        predictionRepo.save(anchorRoundId, outputs)
        val t5 = System.nanoTime()

        // Combine via adaptive ensemble
        val combined = adaptive.combine(
            roundId = anchorRoundId,
            epochMs = System.currentTimeMillis(),
            outputs = outputs,
            history = history,
            rollingAccuracies = rollingAccuracies
        )
        val t6 = System.nanoTime()

        Log.d("Predict", "Prediction complete: top=${combined.top1.number} (${"%.1f".format(combined.top1.probability * 100)}%) | " +
            "cache=${(t1 - t0) / 1_000_000}ms features=${(t2 - t1) / 1_000_000}ms " +
            "weights=${(t3 - t2) / 1_000_000}ms models=${(t4 - t3) / 1_000_000}ms " +
            "save=${(t5 - t4) / 1_000_000}ms ensemble=${(t6 - t5) / 1_000_000}ms " +
            "total=${(t6 - t0) / 1_000_000}ms")

        combined
    }
}
