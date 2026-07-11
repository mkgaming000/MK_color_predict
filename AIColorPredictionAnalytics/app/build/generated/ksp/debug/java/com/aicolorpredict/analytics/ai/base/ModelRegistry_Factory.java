package com.aicolorpredict.analytics.ai.base;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ModelRegistry_Factory implements Factory<ModelRegistry> {
  private final Provider<CalibratorFactory> calibratorFactoryProvider;

  public ModelRegistry_Factory(Provider<CalibratorFactory> calibratorFactoryProvider) {
    this.calibratorFactoryProvider = calibratorFactoryProvider;
  }

  @Override
  public ModelRegistry get() {
    return newInstance(calibratorFactoryProvider.get());
  }

  public static ModelRegistry_Factory create(
      Provider<CalibratorFactory> calibratorFactoryProvider) {
    return new ModelRegistry_Factory(calibratorFactoryProvider);
  }

  public static ModelRegistry newInstance(CalibratorFactory calibratorFactory) {
    return new ModelRegistry(calibratorFactory);
  }
}
