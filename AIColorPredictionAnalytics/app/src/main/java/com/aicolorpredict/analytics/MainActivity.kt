package com.aicolorpredict.analytics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aicolorpredict.analytics.ui.nav.AICpNavHost
import com.aicolorpredict.analytics.ui.theme.AICpTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. Edge-to-edge enabled so the glassmorphism surfaces
 * can sit under the status / navigation bars.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.Transparent.value.toInt(), Color.Transparent.value.toInt()),
            navigationBarStyle = SystemBarStyle.auto(Color.Transparent.value.toInt(), Color.Transparent.value.toInt())
        )
        setContent {
            AICpTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AICpNavHost()
                }
            }
        }
    }
}
