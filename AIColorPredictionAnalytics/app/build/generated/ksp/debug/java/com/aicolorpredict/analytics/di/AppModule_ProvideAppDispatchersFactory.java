package com.aicolorpredict.analytics.di;

import com.aicolorpredict.analytics.util.AppDispatchers;
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
public final class AppModule_ProvideAppDispatchersFactory implements Factory<AppDispatchers> {
  @Override
  public AppDispatchers get() {
    return provideAppDispatchers();
  }

  public static AppModule_ProvideAppDispatchersFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AppDispatchers provideAppDispatchers() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAppDispatchers());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideAppDispatchersFactory INSTANCE = new AppModule_ProvideAppDispatchersFactory();
  }
}
