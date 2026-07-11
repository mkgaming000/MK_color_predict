package com.aicolorpredict.analytics.data.repository;

import com.aicolorpredict.analytics.data.local.dao.RoundDao;
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
public final class RoundRepositoryImpl_Factory implements Factory<RoundRepositoryImpl> {
  private final Provider<RoundDao> daoProvider;

  private final Provider<AppDispatchers> dispatchersProvider;

  public RoundRepositoryImpl_Factory(Provider<RoundDao> daoProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    this.daoProvider = daoProvider;
    this.dispatchersProvider = dispatchersProvider;
  }

  @Override
  public RoundRepositoryImpl get() {
    return newInstance(daoProvider.get(), dispatchersProvider.get());
  }

  public static RoundRepositoryImpl_Factory create(Provider<RoundDao> daoProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    return new RoundRepositoryImpl_Factory(daoProvider, dispatchersProvider);
  }

  public static RoundRepositoryImpl newInstance(RoundDao dao, AppDispatchers dispatchers) {
    return new RoundRepositoryImpl(dao, dispatchers);
  }
}
