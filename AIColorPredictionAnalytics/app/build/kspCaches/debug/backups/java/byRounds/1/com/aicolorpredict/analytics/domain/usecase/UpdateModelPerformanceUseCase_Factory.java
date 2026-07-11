package com.aicolorpredict.analytics.domain.usecase;

import com.aicolorpredict.analytics.ai.base.ModelRegistry;
import com.aicolorpredict.analytics.data.repository.PredictionRepository;
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
public final class UpdateModelPerformanceUseCase_Factory implements Factory<UpdateModelPerformanceUseCase> {
  private final Provider<PredictionRepository> predictionRepoProvider;

  private final Provider<ModelRegistry> registryProvider;

  private final Provider<AppDispatchers> dispatchersProvider;

  public UpdateModelPerformanceUseCase_Factory(
      Provider<PredictionRepository> predictionRepoProvider,
      Provider<ModelRegistry> registryProvider, Provider<AppDispatchers> dispatchersProvider) {
    this.predictionRepoProvider = predictionRepoProvider;
    this.registryProvider = registryProvider;
    this.dispatchersProvider = dispatchersProvider;
  }

  @Override
  public UpdateModelPerformanceUseCase get() {
    return newInstance(predictionRepoProvider.get(), registryProvider.get(), dispatchersProvider.get());
  }

  public static UpdateModelPerformanceUseCase_Factory create(
      Provider<PredictionRepository> predictionRepoProvider,
      Provider<ModelRegistry> registryProvider, Provider<AppDispatchers> dispatchersProvider) {
    return new UpdateModelPerformanceUseCase_Factory(predictionRepoProvider, registryProvider, dispatchersProvider);
  }

  public static UpdateModelPerformanceUseCase newInstance(PredictionRepository predictionRepo,
      ModelRegistry registry, AppDispatchers dispatchers) {
    return new UpdateModelPerformanceUseCase(predictionRepo, registry, dispatchers);
  }
}
