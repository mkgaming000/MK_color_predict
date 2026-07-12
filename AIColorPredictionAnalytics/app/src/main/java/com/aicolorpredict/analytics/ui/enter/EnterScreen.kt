package com.aicolorpredict.analytics.ui.enter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicolorpredict.analytics.domain.model.AppColor
import com.aicolorpredict.analytics.ui.theme.NumberGreen
import com.aicolorpredict.analytics.ui.theme.NumberRed

@Composable
fun EnterScreen(vm: EnterViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.savedColor) {
        state.savedColor?.let {
            snackbarHostState.showSnackbar("${it.display} saved — prediction updated")
            vm.consumeSaved()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.consumeError()
        }
    }

    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar { Text(it.visuals.message) } } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Enter Result", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Tap a color to save instantly.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (state.isSaving) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(16.dp))
                    Text("Saving…", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Two huge color buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ColorButton(
                    color = AppColor.RED,
                    barColor = NumberRed,
                    enabled = !state.isSaving,
                    onClick = { vm.saveColor(AppColor.RED) },
                    modifier = Modifier.weight(1f)
                )
                ColorButton(
                    color = AppColor.GREEN,
                    barColor = NumberGreen,
                    enabled = !state.isSaving,
                    onClick = { vm.saveColor(AppColor.GREEN) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ColorButton(
    color: AppColor,
    barColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(barColor.copy(alpha = if (enabled) 0.85f else 0.3f))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            color.display,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
