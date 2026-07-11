package com.aicolorpredict.analytics.ui.transition;

import com.aicolorpredict.analytics.domain.usecase.SearchUseCase;
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
public final class TransitionViewModel_Factory implements Factory<TransitionViewModel> {
  private final Provider<SearchUseCase> searchUseCaseProvider;

  public TransitionViewModel_Factory(Provider<SearchUseCase> searchUseCaseProvider) {
    this.searchUseCaseProvider = searchUseCaseProvider;
  }

  @Override
  public TransitionViewModel get() {
    return newInstance(searchUseCaseProvider.get());
  }

  public static TransitionViewModel_Factory create(Provider<SearchUseCase> searchUseCaseProvider) {
    return new TransitionViewModel_Factory(searchUseCaseProvider);
  }

  public static TransitionViewModel newInstance(SearchUseCase searchUseCase) {
    return new TransitionViewModel(searchUseCase);
  }
}
