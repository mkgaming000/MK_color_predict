package com.aicolorpredict.analytics.ui.models;

import com.aicolorpredict.analytics.ai.base.ModelRegistry;
import com.aicolorpredict.analytics.data.repository.PredictionRepository;
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
public final class ModelsViewModel_Factory implements Factory<ModelsViewModel> {
  private final Provider<ModelRegistry> registryProvider;

  private final Provider<PredictionRepository> predictionRepoProvider;

  public ModelsViewModel_Factory(Provider<ModelRegistry> registryProvider,
      Provider<PredictionRepository> predictionRepoProvider) {
    this.registryProvider = registryProvider;
    this.predictionRepoProvider = predictionRepoProvider;
  }

  @Override
  public ModelsViewModel get() {
    return newInstance(registryProvider.get(), predictionRepoProvider.get());
  }

  public static ModelsViewModel_Factory create(Provider<ModelRegistry> registryProvider,
      Provider<PredictionRepository> predictionRepoProvider) {
    return new ModelsViewModel_Factory(registryProvider, predictionRepoProvider);
  }

  public static ModelsViewModel newInstance(ModelRegistry registry,
      PredictionRepository predictionRepo) {
    return new ModelsViewModel(registry, predictionRepo);
  }
}
