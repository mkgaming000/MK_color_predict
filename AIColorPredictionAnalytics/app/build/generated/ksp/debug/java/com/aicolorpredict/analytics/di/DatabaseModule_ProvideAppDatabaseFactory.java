package com.aicolorpredict.analytics.di;

import android.content.Context;
import com.aicolorpredict.analytics.data.local.AppDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideAppDatabaseFactory implements Factory<AppDatabase> {
  private final Provider<Context> ctxProvider;

  public DatabaseModule_ProvideAppDatabaseFactory(Provider<Context> ctxProvider) {
    this.ctxProvider = ctxProvider;
  }

  @Override
  public AppDatabase get() {
    return provideAppDatabase(ctxProvider.get());
  }

  public static DatabaseModule_ProvideAppDatabaseFactory create(Provider<Context> ctxProvider) {
    return new DatabaseModule_ProvideAppDatabaseFactory(ctxProvider);
  }

  public static AppDatabase provideAppDatabase(Context ctx) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAppDatabase(ctx));
  }
}
