package com.aicolorpredict.analytics.di

import android.content.Context
import androidx.room.Room
import com.aicolorpredict.analytics.data.local.AppDatabase
import com.aicolorpredict.analytics.data.local.dao.ModelPerformanceDao
import com.aicolorpredict.analytics.data.local.dao.PredictionDao
import com.aicolorpredict.analytics.data.local.dao.RoundDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideRoundDao(db: AppDatabase): RoundDao = db.roundDao()

    @Provides
    fun providePredictionDao(db: AppDatabase): PredictionDao = db.predictionDao()

    @Provides
    fun provideModelPerformanceDao(db: AppDatabase): ModelPerformanceDao = db.modelPerformanceDao()
}
