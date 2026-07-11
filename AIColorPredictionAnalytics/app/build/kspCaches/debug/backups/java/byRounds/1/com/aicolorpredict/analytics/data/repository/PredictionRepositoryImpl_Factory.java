package com.aicolorpredict.analytics.data.repository;

import com.aicolorpredict.analytics.data.local.dao.ModelPerformanceDao;
import com.aicolorpredict.analytics.data.local.dao.PredictionDao;
import com.aicolorpredict.analytics.util.AppDispatchers;
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
public final class PredictionRepositoryImpl_Factory implements Factory<PredictionRepositoryImpl> {
  private final Provider<PredictionDao> predictionDaoProvider;

  private final Provider<ModelPerformanceDao> performanceDaoProvider;

  private final Provider<AppDispatchers> dispatchersProvider;

  public PredictionRepositoryImpl_Factory(Provider<PredictionDao> predictionDaoProvider,
      Provider<ModelPerformanceDao> performanceDaoProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    this.predictionDaoProvider = predictionDaoProvider;
    this.performanceDaoProvider = performanceDaoProvider;
    this.dispatchersProvider = dispatchersProvider;
  }

  @Override
  public PredictionRepositoryImpl get() {
    return newInstance(predictionDaoProvider.get(), performanceDaoProvider.get(), dispatchersProvider.get());
  }

  public static PredictionRepositoryImpl_Factory create(
      Provider<PredictionDao> predictionDaoProvider,
      Provider<ModelPerformanceDao> performanceDaoProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    return new PredictionRepositoryImpl_Factory(predictionDaoProvider, performanceDaoProvider, dispatchersProvider);
  }

  public static PredictionRepositoryImpl newInstance(PredictionDao predictionDao,
      ModelPerformanceDao performanceDao, AppDispatchers dispatchers) {
    return new PredictionRepositoryImpl(predictionDao, performanceDao, dispatchers);
  }
}
