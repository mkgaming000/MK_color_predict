package com.aicolorpredict.analytics.ui.analytics;

import com.aicolorpredict.analytics.data.repository.PredictionRepository;
import com.aicolorpredict.analytics.data.repository.RoundRepository;
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
public final class AnalyticsViewModel_Factory implements Factory<AnalyticsViewModel> {
  private final Provider<RoundRepository> roundRepoProvider;

  private final Provider<PredictionRepository> predictionRepoProvider;

  public AnalyticsViewModel_Factory(Provider<RoundRepository> roundRepoProvider,
      Provider<PredictionRepository> predictionRepoProvider) {
    this.roundRepoProvider = roundRepoProvider;
    this.predictionRepoProvider = predictionRepoProvider;
  }

  @Override
  public AnalyticsViewModel get() {
    return newInstance(roundRepoProvider.get(), predictionRepoProvider.get());
  }

  public static AnalyticsViewModel_Factory create(Provider<RoundRepository> roundRepoProvider,
      Provider<PredictionRepository> predictionRepoProvider) {
    return new AnalyticsViewModel_Factory(roundRepoProvider, predictionRepoProvider);
  }

  public static AnalyticsViewModel newInstance(RoundRepository roundRepo,
      PredictionRepository predictionRepo) {
    return new AnalyticsViewModel(roundRepo, predictionRepo);
  }
}
