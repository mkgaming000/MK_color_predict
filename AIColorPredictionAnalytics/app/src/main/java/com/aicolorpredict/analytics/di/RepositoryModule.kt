package com.aicolorpredict.analytics.di

import com.aicolorpredict.analytics.ai.base.CalibratorFactory
import com.aicolorpredict.analytics.ai.ensemble.AdaptiveWeightingModel
import com.aicolorpredict.analytics.ai.ensemble.EnsembleModel
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.PredictionRepositoryImpl
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.data.repository.RoundRepositoryImpl
import com.aicolorpredict.analytics.feature.FeatureEngineer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindRoundRepo(impl: RoundRepositoryImpl): RoundRepository

    @Binds @Singleton
    abstract fun bindPredictionRepo(impl: PredictionRepositoryImpl): PredictionRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides @Singleton
    fun provideFeatureEngineer(): FeatureEngineer = FeatureEngineer()

    /**
     * Provides the [EnsembleModel] wired to the shared [CalibratorFactory] so
     * per-model Platt/binning calibrators are actually applied during ensemble
     * combination (not just maintained in isolation).
     */
    @Provides @Singleton
    fun provideEnsembleModel(calibratorFactory: CalibratorFactory): EnsembleModel =
        EnsembleModel(calibratorLookup = { name -> calibratorFactory.get(name) })

    @Provides @Singleton
    fun provideAdaptiveEnsemble(ensemble: EnsembleModel): AdaptiveWeightingModel =
        AdaptiveWeightingModel(ensemble)
}
