package com.aicolorpredict.analytics.ui.data;

import android.app.Application;
import com.aicolorpredict.analytics.data.backup.BackupRestoreManager;
import com.aicolorpredict.analytics.data.repository.PredictionRepository;
import com.aicolorpredict.analytics.data.repository.RoundRepository;
import com.aicolorpredict.analytics.domain.usecase.AddRoundUseCase;
import com.aicolorpredict.analytics.domain.usecase.UpdateModelPerformanceUseCase;
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
public final class DataViewModel_Factory implements Factory<DataViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<RoundRepository> roundRepoProvider;

  private final Provider<PredictionRepository> predictionRepoProvider;

  private final Provider<AddRoundUseCase> addRoundUseCaseProvider;

  private final Provider<UpdateModelPerformanceUseCase> updatePerformanceUseCaseProvider;

  private final Provider<BackupRestoreManager> backupRestoreProvider;

  private final Provider<AppDispatchers> dispatchersProvider;

  public DataViewModel_Factory(Provider<Application> applicationProvider,
      Provider<RoundRepository> roundRepoProvider,
      Provider<PredictionRepository> predictionRepoProvider,
      Provider<AddRoundUseCase> addRoundUseCaseProvider,
      Provider<UpdateModelPerformanceUseCase> updatePerformanceUseCaseProvider,
      Provider<BackupRestoreManager> backupRestoreProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    this.applicationProvider = applicationProvider;
    this.roundRepoProvider = roundRepoProvider;
    this.predictionRepoProvider = predictionRepoProvider;
    this.addRoundUseCaseProvider = addRoundUseCaseProvider;
    this.updatePerformanceUseCaseProvider = updatePerformanceUseCaseProvider;
    this.backupRestoreProvider = backupRestoreProvider;
    this.dispatchersProvider = dispatchersProvider;
  }

  @Override
  public DataViewModel get() {
    return newInstance(applicationProvider.get(), roundRepoProvider.get(), predictionRepoProvider.get(), addRoundUseCaseProvider.get(), updatePerformanceUseCaseProvider.get(), backupRestoreProvider.get(), dispatchersProvider.get());
  }

  public static DataViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<RoundRepository> roundRepoProvider,
      Provider<PredictionRepository> predictionRepoProvider,
      Provider<AddRoundUseCase> addRoundUseCaseProvider,
      Provider<UpdateModelPerformanceUseCase> updatePerformanceUseCaseProvider,
      Provider<BackupRestoreManager> backupRestoreProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    return new DataViewModel_Factory(applicationProvider, roundRepoProvider, predictionRepoProvider, addRoundUseCaseProvider, updatePerformanceUseCaseProvider, backupRestoreProvider, dispatchersProvider);
  }

  public static DataViewModel newInstance(Application application, RoundRepository roundRepo,
      PredictionRepository predictionRepo, AddRoundUseCase addRoundUseCase,
      UpdateModelPerformanceUseCase updatePerformanceUseCase, BackupRestoreManager backupRestore,
      AppDispatchers dispatchers) {
    return new DataViewModel(application, roundRepo, predictionRepo, addRoundUseCase, updatePerformanceUseCase, backupRestore, dispatchers);
  }
}
