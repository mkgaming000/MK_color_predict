package com.aicolorpredict.analytics.domain.usecase;

import com.aicolorpredict.analytics.data.repository.RoundRepository;
import com.aicolorpredict.analytics.util.AppDispatchers;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SearchUseCase_Factory implements Factory<SearchUseCase> {
  private final Provider<RoundRepository> roundRepoProvider;

  private final Provider<AppDispatchers> dispatchersProvider;

  public SearchUseCase_Factory(Provider<RoundRepository> roundRepoProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    this.roundRepoProvider = roundRepoProvider;
    this.dispatchersProvider = dispatchersProvider;
  }

  @Override
  public SearchUseCase get() {
    return newInstance(roundRepoProvider.get(), dispatchersProvider.get());
  }

  public static SearchUseCase_Factory create(Provider<RoundRepository> roundRepoProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    return new SearchUseCase_Factory(roundRepoProvider, dispatchersProvider);
  }

  public static SearchUseCase newInstance(RoundRepository roundRepo, AppDispatchers dispatchers) {
    return new SearchUseCase(roundRepo, dispatchers);
  }
}
