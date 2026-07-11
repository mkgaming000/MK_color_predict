package com.aicolorpredict.analytics.data.backup;

import com.aicolorpredict.analytics.data.local.AppDatabase;
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
public final class BackupRestoreManager_Factory implements Factory<BackupRestoreManager> {
  private final Provider<AppDatabase> dbProvider;

  private final Provider<AppDispatchers> dispatchersProvider;

  public BackupRestoreManager_Factory(Provider<AppDatabase> dbProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    this.dbProvider = dbProvider;
    this.dispatchersProvider = dispatchersProvider;
  }

  @Override
  public BackupRestoreManager get() {
    return newInstance(dbProvider.get(), dispatchersProvider.get());
  }

  public static BackupRestoreManager_Factory create(Provider<AppDatabase> dbProvider,
      Provider<AppDispatchers> dispatchersProvider) {
    return new BackupRestoreManager_Factory(dbProvider, dispatchersProvider);
  }

  public static BackupRestoreManager newInstance(AppDatabase db, AppDispatchers dispatchers) {
    return new BackupRestoreManager(db, dispatchers);
  }
}
