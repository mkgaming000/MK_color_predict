package com.aicolorpredict.analytics.data.backup

import com.aicolorpredict.analytics.data.local.AppDatabase
import com.aicolorpredict.analytics.data.local.entity.ModelPerformanceEntity
import com.aicolorpredict.analytics.data.local.entity.PredictionEntity
import com.aicolorpredict.analytics.data.local.entity.RoundEntity
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-level backup / restore facade used by the Data screen. Writes go through
 * the [BackupManager] JSON format; restore swaps the in-DB contents atomically
 * (under a Room transaction) so a failed restore cannot leave the DB half-written.
 */
@Singleton
class BackupRestoreManager @Inject constructor(
    private val db: AppDatabase,
    private val dispatchers: AppDispatchers
) {
    suspend fun export(out: OutputStream) = withContext(dispatchers.io) {
        val rounds = db.roundDao().getAll()
        val perf = db.modelPerformanceDao().getAll()
        // For predictions we only take the most recent 5000 rounds to keep
        // backups sane. (Older predictions remain in the DB; this is purely a
        // backup-size cap.)
        val predictions = mutableListOf<PredictionEntity>()
        for (r in rounds.takeLast(5000)) {
            predictions += db.predictionDao().getByRound(r.id)
        }
        BackupManager.write(BackupManager.Bundle(rounds, predictions, perf), out)
    }

    suspend fun restore(input: InputStream) = withContext(dispatchers.io) {
        val bundle = BackupManager.read(input)
        db.runInTransaction {
            db.roundDao().clearAll()
            db.predictionDao().clearAll()
            db.modelPerformanceDao().clearAll()
            db.roundDao().insertAll(bundle.rounds)
            db.predictionDao().insertAll(bundle.predictions)
            db.modelPerformanceDao().upsertAll(bundle.performance)
        }
    }
}
