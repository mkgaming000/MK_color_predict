package com.aicolorpredict.analytics.di;

import com.aicolorpredict.analytics.ai.ensemble.AdaptiveWeightingModel;
import com.aicolorpredict.analytics.ai.ensemble.EnsembleModel;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DomainModule_ProvideAdaptiveEnsembleFactory implements Factory<AdaptiveWeightingModel> {
  private final Provider<EnsembleModel> ensembleProvider;

  public DomainModule_ProvideAdaptiveEnsembleFactory(Provider<EnsembleModel> ensembleProvider) {
    this.ensembleProvider = ensembleProvider;
  }

  @Override
  public AdaptiveWeightingModel get() {
    return provideAdaptiveEnsemble(ensembleProvider.get());
  }

  public static DomainModule_ProvideAdaptiveEnsembleFactory create(
      Provider<EnsembleModel> ensembleProvider) {
    return new DomainModule_ProvideAdaptiveEnsembleFactory(ensembleProvider);
  }

  public static AdaptiveWeightingModel provideAdaptiveEnsemble(EnsembleModel ensemble) {
    return Preconditions.checkNotNullFromProvides(DomainModule.INSTANCE.provideAdaptiveEnsemble(ensemble));
  }
}
