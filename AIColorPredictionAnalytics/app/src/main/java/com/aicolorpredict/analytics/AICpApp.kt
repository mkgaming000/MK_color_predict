package com.aicolorpredict.analytics

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point.
 *
 * Hilt's @HiltAndroidApp triggers the generated dependency graph at app start.
 * We deliberately keep this class thin — all configuration is delegated to
 * the DI modules in `com.aicolorpredict.analytics.di`.
 */
@HiltAndroidApp
class AICpApp : Application()
