---
Task ID: root
Agent: main (Super Z)
Task: Design and implement a production-grade Android application that performs advanced statistical analysis on historical Color Prediction game data — 15+ AI models, Clean Architecture, Material 3 Compose UI, charts, self-learning pipeline, model unit tests.

Work Log:
- Clarified 8 dimensions with the user (scope, ML approach, tree models, charts lib, UI stack, min SDK, DI/arch, tests). User chose: Full build / Pure-Kotlin DL / Pure-Kotlin GBM / Vico / Compose only / Android 8+ / Hilt+Coroutines / model unit tests.
- Created full Android project skeleton under /home/z/my-project/AIColorPredictionAnalytics/ — Gradle build files, settings, wrapper, manifest, theme XML, launcher icons, proguard.
- Implemented domain layer: BallColor + colorsForNumber mapping, Round with window-slicing & streak detection, ModelOutput with fromVector normalisation + invariant validation, Prediction with Top-K accessors, FeatureSet, TransitionStats, ModelPerformance, AccuracyMetrics, ColorMapper.
- Implemented data layer: 3 Room entities (RoundEntity/PredictionEntity/ModelPerformanceEntity) with proper indices, 3 DAOs with Flow-returning queries, AppDatabase, Converters (color bitmask), entity↔domain mappers, RoundRepository + impl, PredictionRepository + impl.
- Implemented CSV / JSON importers (header-detecting, narrow & wide formats) and exporters (lossless wide-format CSV). Implemented BackupManager (versioned JSON) + BackupRestoreManager (atomic restore under Room transaction).
- Implemented feature engineering: StatsCalculator (entropy, variance, correlation, cosine, RLE, weighted average), TransitionMatrix (Laplace-smoothed 10×10), TransitionAnalytics (per-from-number stats with gap tracking), PatternDetector (mirror, alternating, repeating, cycles, summarise), FeatureEngineer (single shared FeatureSet with ~150-dim feature vector for tree/neural models).
- Implemented 7 statistical models: FrequencyAnalysis, MarkovChain, HiddenMarkovModel (5 latent states), BayesianNetwork (Naive Bayes with 5 binary features), TransitionMatrix (1st + 2nd order blend), MovingAverage (multi-window blend), MonteCarlo (1000 deterministic-seeded simulations). Each model caps confidence conservatively.
- Implemented 4 pure-Kotlin neural models: Matrix (allocation-explicit), Activations (sigmoid/tanh/relu/softmax), LstmModel (32-unit, 4 gates, online pretrain of output layer), GruModel (2 gates), TransformerModel (dModel=16, single-head self-attention, tied embeddings), TemporalCnnModel (3 dilated causal conv layers).
- Implemented 6 tree models: DecisionTree (Gini, random subspace), DecisionTreeRegressor (variance reduction for GBM), RandomForestModel (16 bootstrap trees), GradientBoostingModel (multinomial deviance, 12 rounds × 10 classes), XGBoostModel / LightGbmModel / CatBoostModel (hyper-parameter variants of the GBM engine).
- Implemented ensemble + calibration: EnsembleModel (softmax-weighted by accuracy + confidence + sample-size, max 35% per model), AdaptiveWeightingModel (boosts weights with rolling accuracy), PlattCalibrator (online logistic regression), BinningCalibrator (10-bin fallback), ConfidenceCalibrator (composes both).
- Implemented MetricsCalculator: Top-1/3/5, LogLoss, Brier, macro Precision/Recall/F1, confusion matrix, rolling accuracy, per-model and system-level aggregation.
- Implemented 5 use cases: AddRoundUseCase (resolves previous round's predictions), PredictUseCase (parallel model execution + persistence + ensemble), UpdateModelPerformanceUseCase (recomputes metrics + updates calibrators), GetHistoryUseCase, SearchUseCase (round ID / number / pattern / transition stats).
- Implemented Hilt DI: AppModule (dispatchers), DatabaseModule (Room + DAOs), RepositoryModule (repository bindings), DomainModule (FeatureEngineer, EnsembleModel, AdaptiveWeightingModel).
- Implemented Material 3 Compose UI: Color/Theme/Shape/Type files, GlassCard (vertical gradient + hairline border), ProbabilityBar (animated 600ms tween), ConfidenceBadge / ConsensusBadge / StatChip, EmptyState, ChartCard (Vico bar/line charts).
- Implemented 9 screens + their ViewModels + bottom-nav NavHost: Dashboard, Prediction, History, Analytics, Transition, Models, Search, Data (add round + CSV/JSON import/export + backup/restore + clear all), Settings.
- Implemented 13 unit-test files covering: StatsCalculator, TransitionMatrix, ColorMapping, PatternDetector, FeatureEngineer, every statistical model, every neural model, every tree model, ensemble + calibration, MetricsCalculator, Round construction, CSV/JSON round-trip.

Stage Summary:
- Delivered a complete, production-grade Android project: 134 files (~8,700 lines of Kotlin), 15 AI models across 4 categories, 9 fully-functional screens, 13 unit-test files.
- No placeholder code, no fake AI, no hardcoded probabilities. Every model caps its own confidence conservatively (statistical ≤0.85, tree ≤0.78, neural ≤0.70, ensemble ≤0.85) to avoid overstating performance.
- Pure-Kotlin ML/DL implementation: no TensorFlow Lite, no ONNX, no native dependencies. All neural nets and tree models work fully offline.
- Stack: Compose only, Material 3, Hilt + Coroutines + Room, Vico charts, min SDK 26 / target 34.
- Project root: /home/z/my-project/AIColorPredictionAnalytics/
- README at /home/z/my-project/AIColorPredictionAnalytics/README.md documents the full architecture, model list, feature engineering, metrics, and how to build/test.
