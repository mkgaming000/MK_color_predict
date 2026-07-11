package com.aicolorpredict.analytics.di;

import com.aicolorpredict.analytics.feature.FeatureEngineer;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DomainModule_ProvideFeatureEngineerFactory implements Factory<FeatureEngineer> {
  @Override
  public FeatureEngineer get() {
    return provideFeatureEngineer();
  }

  public static DomainModule_ProvideFeatureEngineerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FeatureEngineer provideFeatureEngineer() {
    return Preconditions.checkNotNullFromProvides(DomainModule.INSTANCE.provideFeatureEngineer());
  }

  private static final class InstanceHolder {
    private static final DomainModule_ProvideFeatureEngineerFactory INSTANCE = new DomainModule_ProvideFeatureEngineerFactory();
  }
}
