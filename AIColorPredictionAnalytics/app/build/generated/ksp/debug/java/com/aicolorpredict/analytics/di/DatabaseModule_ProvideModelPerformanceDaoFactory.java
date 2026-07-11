package com.aicolorpredict.analytics.di;

import com.aicolorpredict.analytics.data.local.AppDatabase;
import com.aicolorpredict.analytics.data.local.dao.ModelPerformanceDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideModelPerformanceDaoFactory implements Factory<ModelPerformanceDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideModelPerformanceDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ModelPerformanceDao get() {
    return provideModelPerformanceDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideModelPerformanceDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideModelPerformanceDaoFactory(dbProvider);
  }

  public static ModelPerformanceDao provideModelPerformanceDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideModelPerformanceDao(db));
  }
}
