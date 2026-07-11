package com.aicolorpredict.analytics.di;

import com.aicolorpredict.analytics.data.local.AppDatabase;
import com.aicolorpredict.analytics.data.local.dao.PredictionDao;
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
public final class DatabaseModule_ProvidePredictionDaoFactory implements Factory<PredictionDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvidePredictionDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PredictionDao get() {
    return providePredictionDao(dbProvider.get());
  }

  public static DatabaseModule_ProvidePredictionDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvidePredictionDaoFactory(dbProvider);
  }

  public static PredictionDao providePredictionDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePredictionDao(db));
  }
}
