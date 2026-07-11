package com.aicolorpredict.analytics.ui.search;

import com.aicolorpredict.analytics.domain.usecase.SearchUseCase;
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
public final class SearchViewModel_Factory implements Factory<SearchViewModel> {
  private final Provider<SearchUseCase> searchUseCaseProvider;

  private final Provider<AppDispatchers> dispatchersProvider;

  public SearchViewModel_Factory(Provider<SearchUseCase> searchUseCaseProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    this.searchUseCaseProvider = searchUseCaseProvider;
    this.dispatchersProvider = dispatchersProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(searchUseCaseProvider.get(), dispatchersProvider.get());
  }

  public static SearchViewModel_Factory create(Provider<SearchUseCase> searchUseCaseProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    return new SearchViewModel_Factory(searchUseCaseProvider, dispatchersProvider);
  }

  public static SearchViewModel newInstance(SearchUseCase searchUseCase,
      AppDispatchers dispatchers) {
    return new SearchViewModel(searchUseCase, dispatchers);
  }
}
