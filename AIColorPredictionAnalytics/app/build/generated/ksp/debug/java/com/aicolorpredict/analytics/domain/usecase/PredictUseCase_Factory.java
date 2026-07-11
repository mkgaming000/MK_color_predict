package com.aicolorpredict.analytics.domain.usecase;

import com.aicolorpredict.analytics.ai.base.ModelRegistry;
import com.aicolorpredict.analytics.ai.ensemble.AdaptiveWeightingModel;
import com.aicolorpredict.analytics.ai.ensemble.EnsembleModel;
import com.aicolorpredict.analytics.data.repository.PredictionRepository;
import com.aicolorpredict.analytics.data.repository.RoundRepository;
import com.aicolorpredict.analytics.feature.FeatureEngineer;
import com.aicolorpredict.analytics.util.AppDispatchers;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class PredictUseCase_Factory implements Factory<PredictUseCase> {
  private final Provider<RoundRepository> roundRepoProvider;

  private final Provider<PredictionRepository> predictionRepoProvider;

  private final Provider<ModelRegistry> registryProvider;

  private final Provider<FeatureEngineer> featureEngineerProvider;

  private final Provider<EnsembleModel> ensembleProvider;

  private final Provider<AdaptiveWeightingModel> adaptiveProvider;

  private final Provider<AppDispatchers> dispatchersProvider;

  public PredictUseCase_Factory(Provider<RoundRepository> roundRepoProvider,
      Provider<PredictionRepository> predictionRepoProvider,
      Provider<ModelRegistry> registryProvider, Provider<FeatureEngineer> featureEngineerProvider,
      Provider<EnsembleModel> ensembleProvider, Provider<AdaptiveWeightingModel> adaptiveProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    this.roundRepoProvider = roundRepoProvider;
    this.predictionRepoProvider = predictionRepoProvider;
    this.registryProvider = registryProvider;
    this.featureEngineerProvider = featureEngineerProvider;
    this.ensembleProvider = ensembleProvider;
    this.adaptiveProvider = adaptiveProvider;
    this.dispatchersProvider = dispatchersProvider;
  }

  @Override
  public PredictUseCase get() {
    return newInstance(roundRepoProvider.get(), predictionRepoProvider.get(), registryProvider.get(), featureEngineerProvider.get(), ensembleProvider.get(), adaptiveProvider.get(), dispatchersProvider.get());
  }

  public static PredictUseCase_Factory create(Provider<RoundRepository> roundRepoProvider,
      Provider<PredictionRepository> predictionRepoProvider,
      Provider<ModelRegistry> registryProvider, Provider<FeatureEngineer> featureEngineerProvider,
      Provider<EnsembleModel> ensembleProvider, Provider<AdaptiveWeightingModel> adaptiveProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    return new PredictUseCase_Factory(roundRepoProvider, predictionRepoProvider, registryProvider, featureEngineerProvider, ensembleProvider, adaptiveProvider, dispatchersProvider);
  }

  public static PredictUseCase newInstance(RoundRepository roundRepo,
      PredictionRepository predictionRepo, ModelRegistry registry, FeatureEngineer featureEngineer,
      EnsembleModel ensemble, AdaptiveWeightingModel adaptive, AppDispatchers dispatchers) {
    return new PredictUseCase(roundRepo, predictionRepo, registry, featureEngineer, ensemble, adaptive, dispatchers);
  }
}
