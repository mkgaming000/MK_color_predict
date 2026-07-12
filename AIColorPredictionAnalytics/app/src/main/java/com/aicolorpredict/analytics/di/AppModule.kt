package com.aicolorpredict.analytics.di

import android.content.Context
import androidx.room.Room
import com.aicolorpredict.analytics.ai.color.ColorAdaptiveWeightingEngine
import com.aicolorpredict.analytics.ai.color.ColorEnsembleModel
import com.aicolorpredict.analytics.ai.color.ColorModelRegistry
import com.aicolorpredict.analytics.data.local.AppDatabase
import com.aicolorpredict.analytics.data.local.dao.ColorModelPerformanceDao
import com.aicolorpredict.analytics.data.local.dao.ColorPredictionDao
import com.aicolorpredict.analytics.data.local.dao.ColorRoundDao
import com.aicolorpredict.analytics.data.repository.ColorPredictionRepository
import com.aicolorpredict.analytics.data.repository.ColorPredictionRepositoryImpl
import com.aicolorpredict.analytics.data.repository.ColorRoundRepository
import com.aicolorpredict.analytics.data.repository.ColorRoundRepositoryImpl
import com.aicolorpredict.analytics.util.AppDispatchers
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideAppDispatchers(): AppDispatchers = AppDispatchers(
        io = Dispatchers.IO, default = Dispatchers.Default, main = Dispatchers.Main
    )
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideColorRoundDao(db: AppDatabase): ColorRoundDao = db.colorRoundDao()
    @Provides fun provideColorPredictionDao(db: AppDatabase): ColorPredictionDao = db.colorPredictionDao()
    @Provides fun provideColorModelPerformanceDao(db: AppDatabase): ColorModelPerformanceDao = db.colorModelPerformanceDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindColorRoundRepo(impl: ColorRoundRepositoryImpl): ColorRoundRepository

    @Binds @Singleton
    abstract fun bindColorPredictionRepo(impl: ColorPredictionRepositoryImpl): ColorPredictionRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides @Singleton
    fun provideColorEnsembleModel(): ColorEnsembleModel = ColorEnsembleModel()
}
