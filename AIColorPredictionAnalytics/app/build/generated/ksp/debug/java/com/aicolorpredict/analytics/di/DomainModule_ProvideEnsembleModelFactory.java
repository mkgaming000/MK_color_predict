package com.aicolorpredict.analytics.di;

import com.aicolorpredict.analytics.ai.base.CalibratorFactory;
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
public final class DomainModule_ProvideEnsembleModelFactory implements Factory<EnsembleModel> {
  private final Provider<CalibratorFactory> calibratorFactoryProvider;

  public DomainModule_ProvideEnsembleModelFactory(
      Provider<CalibratorFactory> calibratorFactoryProvider) {
    this.calibratorFactoryProvider = calibratorFactoryProvider;
  }

  @Override
  public EnsembleModel get() {
    return provideEnsembleModel(calibratorFactoryProvider.get());
  }

  public static DomainModule_ProvideEnsembleModelFactory create(
      Provider<CalibratorFactory> calibratorFactoryProvider) {
    return new DomainModule_ProvideEnsembleModelFactory(calibratorFactoryProvider);
  }

  public static EnsembleModel provideEnsembleModel(CalibratorFactory calibratorFactory) {
    return Preconditions.checkNotNullFromProvides(DomainModule.INSTANCE.provideEnsembleModel(calibratorFactory));
  }
}
