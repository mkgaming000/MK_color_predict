package com.aicolorpredict.analytics.ui.dashboard;

import com.aicolorpredict.analytics.data.repository.PredictionRepository;
import com.aicolorpredict.analytics.data.repository.RoundRepository;
import com.aicolorpredict.analytics.domain.usecase.PredictUseCase;
import com.aicolorpredict.analytics.domain.usecase.UpdateModelPerformanceUseCase;
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<RoundRepository> roundRepoProvider;

  private final Provider<PredictionRepository> predictionRepoProvider;

  private final Provider<PredictUseCase> predictUseCaseProvider;

  private final Provider<UpdateModelPerformanceUseCase> updatePerformanceUseCaseProvider;

  public DashboardViewModel_Factory(Provider<RoundRepository> roundRepoProvider,
      Provider<PredictionRepository> predictionRepoProvider,
      Provider<PredictUseCase> predictUseCaseProvider,
      Provider<UpdateModelPerformanceUseCase> updatePerformanceUseCaseProvider) {
    this.roundRepoProvider = roundRepoProvider;
    this.predictionRepoProvider = predictionRepoProvider;
    this.predictUseCaseProvider = predictUseCaseProvider;
    this.updatePerformanceUseCaseProvider = updatePerformanceUseCaseProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(roundRepoProvider.get(), predictionRepoProvider.get(), predictUseCaseProvider.get(), updatePerformanceUseCaseProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<RoundRepository> roundRepoProvider,
      Provider<PredictionRepository> predictionRepoProvider,
      Provider<PredictUseCase> predictUseCaseProvider,
      Provider<UpdateModelPerformanceUseCase> updatePerformanceUseCaseProvider) {
    return new DashboardViewModel_Factory(roundRepoProvider, predictionRepoProvider, predictUseCaseProvider, updatePerformanceUseCaseProvider);
  }

  public static DashboardViewModel newInstance(RoundRepository roundRepo,
      PredictionRepository predictionRepo, PredictUseCase predictUseCase,
      UpdateModelPerformanceUseCase updatePerformanceUseCase) {
    return new DashboardViewModel(roundRepo, predictionRepo, predictUseCase, updatePerformanceUseCase);
  }
}
