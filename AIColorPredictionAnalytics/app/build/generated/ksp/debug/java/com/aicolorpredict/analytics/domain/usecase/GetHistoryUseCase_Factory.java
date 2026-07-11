package com.aicolorpredict.analytics.domain.usecase;

import com.aicolorpredict.analytics.data.repository.PredictionRepository;
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
public final class GetHistoryUseCase_Factory implements Factory<GetHistoryUseCase> {
  private final Provider<RoundRepository> roundRepoProvider;

  private final Provider<PredictionRepository> predictionRepoProvider;

  private final Provider<AppDispatchers> dispatchersProvider;

  public GetHistoryUseCase_Factory(Provider<RoundRepository> roundRepoProvider,
      Provider<PredictionRepository> predictionRepoProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    this.roundRepoProvider = roundRepoProvider;
    this.predictionRepoProvider = predictionRepoProvider;
    this.dispatchersProvider = dispatchersProvider;
  }

  @Override
  public GetHistoryUseCase get() {
    return newInstance(roundRepoProvider.get(), predictionRepoProvider.get(), dispatchersProvider.get());
  }

  public static GetHistoryUseCase_Factory create(Provider<RoundRepository> roundRepoProvider,
      Provider<PredictionRepository> predictionRepoProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    return new GetHistoryUseCase_Factory(roundRepoProvider, predictionRepoProvider, dispatchersProvider);
  }

  public static GetHistoryUseCase newInstance(RoundRepository roundRepo,
      PredictionRepository predictionRepo, AppDispatchers dispatchers) {
    return new GetHistoryUseCase(roundRepo, predictionRepo, dispatchers);
  }
}
