package com.aicolorpredict.analytics.ui.prediction;

import com.aicolorpredict.analytics.domain.usecase.PredictUseCase;
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
public final class PredictionViewModel_Factory implements Factory<PredictionViewModel> {
  private final Provider<PredictUseCase> predictUseCaseProvider;

  public PredictionViewModel_Factory(Provider<PredictUseCase> predictUseCaseProvider) {
    this.predictUseCaseProvider = predictUseCaseProvider;
  }

  @Override
  public PredictionViewModel get() {
    return newInstance(predictUseCaseProvider.get());
  }

  public static PredictionViewModel_Factory create(
      Provider<PredictUseCase> predictUseCaseProvider) {
    return new PredictionViewModel_Factory(predictUseCaseProvider);
  }

  public static PredictionViewModel newInstance(PredictUseCase predictUseCase) {
    return new PredictionViewModel(predictUseCase);
  }
}
