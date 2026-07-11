package com.aicolorpredict.analytics.ai.base;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class CalibratorFactory_Factory implements Factory<CalibratorFactory> {
  @Override
  public CalibratorFactory get() {
    return newInstance();
  }

  public static CalibratorFactory_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CalibratorFactory newInstance() {
    return new CalibratorFactory();
  }

  private static final class InstanceHolder {
    private static final CalibratorFactory_Factory INSTANCE = new CalibratorFactory_Factory();
  }
}
