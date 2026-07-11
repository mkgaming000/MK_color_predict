# AI Color Prediction Analytics

A production-grade Android application that performs advanced statistical analysis on historical Color Prediction game data.

> **Important disclaimer** — every number, percentage, confidence score, and accuracy metric shown in this app is a *statistical estimate derived from historical data*. Random games are by definition unpredictable. This app does not claim to predict outcomes with certainty, does not guarantee any result, and is for educational and informational use only. Every model in the pipeline is calibrated against its own historical performance and reports its confidence conservatively.

## Project stats

- **100+ Kotlin source files** organised by Clean Architecture layer
- **15 AI models** across 4 categories (statistical, tree-based, neural, ensemble)
- **Material 3 / Jetpack Compose** UI with glassmorphism, dark/light themes, and edge-to-edge layout
- **Vico** charts for histograms, line charts, and rolling-accuracy trends
- **Hilt** + **Coroutines/Flow** + **Room** stack
- **Pure-Kotlin neural nets** (LSTM, GRU, Transformer, TCN) — no native dependencies
- **Pure-Kotlin tree models** (Random Forest, GBM, XGBoost/LightGBM/CatBoost variants) — no native dependencies
- **20+ unit tests** covering models, feature engineering, metrics, and import/export
- Min SDK 26 (Android 8), target SDK 34

## Project layout

```
AIColorPredictionAnalytics/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── res/                # themes, strings, colors, icons, file_paths
        │   └── java/com/aicolorpredict/analytics/
        │       ├── AICpApp.kt       # @HiltAndroidApp entry point
        │       ├── MainActivity.kt  # Single activity, edge-to-edge
        │       ├── di/              # Hilt modules (App, Database, Repository, Domain)
        │       ├── data/
        │       │   ├── local/       # Room: entities, DAOs, AppDatabase, Converters
        │       │   ├── repository/  # RoundRepository, PredictionRepository (+ impls)
        │       │   ├── importer/    # CSV/JSON importers & exporters
        │       │   └── backup/      # BackupManager, BackupRestoreManager
        │       ├── domain/
        │       │   ├── model/       # Round, Color, Prediction, ModelOutput, FeatureSet, ...
        │       │   └── usecase/     # AddRound, Predict, UpdatePerformance, GetHistory, Search
        │       ├── feature/         # FeatureEngineer, PatternDetector, TransitionMatrix, StatsCalculator
        │       ├── ai/
        │       │   ├── base/        # PredictionModel interface, ModelRegistry, ModelUtils
        │       │   ├── frequency/   # FrequencyAnalysisModel
        │       │   ├── markov/      # MarkovChainModel, HiddenMarkovModel
        │       │   ├── bayesian/    # BayesianNetworkModel (Naive Bayes)
        │       │   ├── transition/  # TransitionMatrixModel (1st + 2nd order)
        │       │   ├── movingavg/   # MovingAverageModel
        │       │   ├── monte/       # MonteCarloModel
        │       │   ├── tree/        # DecisionTree, RandomForest, GBM, XGB, LGBM, CatBoost
        │       │   ├── neural/      # Matrix, Activations, LSTM, GRU, Transformer, TemporalCNN
        │       │   ├── ensemble/    # EnsembleModel, AdaptiveWeightingModel
        │       │   └── calibration/ # PlattCalibrator, BinningCalibrator, ConfidenceCalibrator
        │       ├── metrics/         # MetricsCalculator (TopK, LogLoss, Brier, P/R/F1, Confusion)
        │       ├── ui/
        │       │   ├── theme/       # Material 3 colors, typography, shapes, theme
        │       │   ├── components/  # GlassCard, ProbabilityBar, ConfidenceBadge, ChartCard, ...
        │       │   ├── nav/         # Bottom-nav destinations + NavHost
        │       │   ├── dashboard/   # Dashboard screen + ViewModel
        │       │   ├── prediction/  # Live prediction screen
        │       │   ├── history/     # Round-by-round history with stored predictions
        │       │   ├── analytics/   # System metrics + per-model comparison
        │       │   ├── transition/  # Transition analysis (prev → next)
        │       │   ├── models/      # Per-model performance cards
        │       │   ├── search/      # Search by round ID, number, or pattern
        │       │   ├── data/        # Add round, CSV/JSON import/export, backup/restore
        │       │   └── settings/    # Theme toggles + disclaimer
        │       └── util/            # AppDispatchers, Resource, DateUtils
        └── test/java/com/aicolorpredict/analytics/
            ├── ai/                  # Statistical/Neural/Tree/Ensemble/Calibration tests
            ├── data/                # CSV/JSON round-trip tests
            ├── domain/              # Round construction tests
            ├── feature/             # Feature engineer + pattern detector tests
            └── metrics/             # MetricsCalculator tests
```

## AI pipeline

```
Data Collection (Room)
   ↓
Cleaning (validation in Round.fromNumber)
   ↓
Feature Engineering (FeatureEngineer)
   ↓
Pattern Recognition (PatternDetector)
   ↓
Probability Calculation (15 models run in parallel)
   ↓
Multi-Model Prediction (per-model ModelOutput)
   ↓
Weighted Ensemble (softmax over accuracy + confidence + sample-size)
   ↓
Confidence Calibration (Platt + Binning)
   ↓
Result Comparison (resolve on next round)
   ↓
Model Learning (per-model rolling metrics)
   ↓
Accuracy Update (persisted to model_performance table)
```

## AI models implemented

### Statistical
- **Frequency Analysis** — Laplace-smoothed empirical distribution; baseline.
- **Markov Chain** — 1st-order transition probabilities with sample-size-aware blending.
- **Hidden Markov Model** — 5 latent states (hot-red, hot-green, balanced, cold-red, cold-green); inferred belief + per-state emissions.
- **Bayesian Network** — Naive Bayes with 5 binary features (parity, color, violet, hot, recent-green-heavy).
- **Transition Matrix** — 1st + 2nd order transitions blended by sample size.
- **Moving Average** — Weighted blend of 5/20/100-round distributions; confidence from cross-window agreement.
- **Monte Carlo Simulation** — 1000 simulated continuations from the empirical transition matrix; deterministic per-seed.

### Tree-based (pure Kotlin)
- **Decision Tree** — Gini impurity, random subspace method, depth-capped.
- **Random Forest** — 16 bootstrap-sampled trees, predictions averaged.
- **Gradient Boosting** — Multinomial deviance loss, 12 rounds × 10 classes.
- **XGBoost** — GBM variant with deeper trees, lower learning rate.
- **LightGBM** — GBM variant with leaf-wise growth (deeper, smaller min split).
- **CatBoost** — GBM variant with symmetric (depth-2) trees, more rounds.

### Neural (pure Kotlin — hand-rolled)
- **Matrix** — Allocation-explicit matrix math, Xavier/He init.
- **LSTM** — 32-unit, 4 gates, online pretrain of output layer.
- **GRU** — 32-unit, 2 gates, faster than LSTM.
- **Transformer** — dModel=16, single-head self-attention, FFN, tied input/output embeddings.
- **Temporal CNN** — 3 dilated causal conv layers (dilations 1/2/4), residual + global average pool.

### Ensemble
- **Ensemble Model** — Softmax-weighted combination; caps any single model's weight at 35%.
- **Adaptive Weighting** — Boosts each model's weight using its *rolling* Top-1 accuracy so in-form models get more say.

### Calibration
- **Platt Calibrator** — Online logistic regression on (raw_confidence, was_correct) pairs.
- **Binning Calibrator** — 10-bin equal-width isotonic-style fallback.
- **Confidence Calibrator** — Composes Platt (after 50 samples) and Binning (after 20 samples).

## Feature engineering

`FeatureEngineer.build()` produces a single `FeatureSet` consumed by every model:

- Number frequency (10 features)
- Color frequency (3 features)
- 10×10 transition matrix + row totals (110 features)
- Per-number gaps (10 features)
- Hot/cold number lists (top-3 / bottom-3)
- Recent (30) and long-term (200) linearly-weighted momentum (20 features)
- Parity / size / color ratios (7 features)
- Rolling averages (10 features)
- Shannon entropy + variance (2 features)
- Pattern frequency (mirror / repeating-sequence counts)
- Cycle detection (top-K substrings by length × count)
- Alternating-color run-length encoding
- Historical similarity (cosine similarity between trailing 20-window and every other 20-window in history) → 10 features
- **Dense feature vector** (~150+ dimensions) consumed by tree and neural models

## Metrics

`MetricsCalculator` is the single source of truth:

- Top-1 / Top-3 / Top-5 accuracy
- Multinomial LogLoss (epsilon-clamped)
- Multiclass Brier score
- Macro precision / recall / F1
- Confusion matrix (actual × predicted)
- Rolling accuracy over the last 100 resolved predictions
- System-level aggregate (sample-weighted average across models)

## Data layer

- **Room** database (`aicp.db`) with three tables: `rounds`, `predictions`, `model_performance`
- Auto-indexed on `epochMs`, `number`, `colors`, `streak`, `roundId`, `modelName`, `correct`
- **CSV / JSON import** with header detection, both wide (with explicit previous windows) and narrow (just `id, time, number`) formats
- **CSV / JSON export** with full round-trip losslessness (covered by unit tests)
- **Backup / Restore** — single JSON file containing rounds + predictions + model_performance, restored atomically under a Room transaction
- **History replay** via the History screen

## UI

- **Material 3** with both dark and light themes
- **Glassmorphism** surfaces (vertical gradient + hairline border; no expensive RenderEffect)
- **9 bottom-nav destinations** with smooth navigation-state preservation
- **Vico** charts: number-frequency bar chart, per-model accuracy line chart, color histograms
- **Animated probability bars** (600ms ease tween on every value change)
- **Edge-to-edge** with transparent system bars
- **Responsive** — works on phones and tablets

## Code-quality guarantees

- No placeholder code; every model and screen is fully implemented
- No fake AI — every probability comes from a real computation over real history
- No hardcoded probabilities — every number is derived from the data
- Every model's confidence is *capped* (statistical ≤ 0.85, tree ≤ 0.78, neural ≤ 0.70, ensemble ≤ 0.85) to prevent overstatement
- Every model output's `numberProbabilities` is validated at construction time to sum to 1.0
- All AI models are pure and stateless — no shared mutable state, no thread-safety hazards
- The single source of truth for "how well is the system doing?" is `MetricsCalculator`; no other module computes its own accuracy
- The disclaimer appears on the dashboard, the prediction screen, and the settings screen

## How to build

```bash
cd AIColorPredictionAnalytics
./gradlew :app:assembleDebug
```

Open in Android Studio Hedgehog (2023.1.1) or newer. JDK 17 required.

## How to use

1. **Add rounds** — open the Data tab and tap any number 0..9, or import a CSV/JSON.
2. **Run a prediction** — open the Dashboard or Predict tab and tap "Run AI Prediction". The full pipeline (15 models + ensemble + calibration) runs in parallel.
3. **Add the actual outcome** — back on the Data tab, tap the number that actually came up. This resolves the previous round's predictions and updates every model's rolling metrics.
4. **Watch the metrics improve** — the Analytics and Models screens update in real time as more rounds are resolved.

## Testing

```bash
./gradlew :app:testDebugUnitTest
```

Test coverage:

- `StatsCalculatorTest` — entropy, variance, correlation, cosine, RLE
- `TransitionMatrixTest` — Laplace smoothing, transition analytics
- `ColorMappingTest` — canonical 0..9 → color mapping
- `PatternDetectorTest` — mirror, alternating, cycle, summarise
- `FeatureEngineerTest` — feature dimensions, hot/cold, entropy, empty-history safety
- `StatisticalModelsTest` — every statistical model produces well-formed output; confidence caps enforced
- `NeuralModelsTest` — LSTM/GRU/Transformer/TCN well-formed output; empty-history fallback
- `TreeModelsTest` — RF/XGB/LGBM/CatBoost well-formed output; insufficient-history fallback
- `EnsembleAndCalibrationTest` — ensemble combination, max-weight cap, Platt & binning calibration
- `MetricsCalculatorTest` — TopK, LogLoss, Brier, confusion matrix, rolling accuracy, macro precision/recall
- `RoundTest` — color flags, window slicing, streak counting, validation
- `ImportExportTest` — CSV/JSON round-trip, narrow format, wide format
