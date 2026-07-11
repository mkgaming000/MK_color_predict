package com.aicolorpredict.analytics.ui.data

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.ui.components.StatChip

@Composable
fun DataScreen(
    vm: DataViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    val csvImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.importCsv(it) }
    }
    val jsonImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.importJson(it) }
    }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { vm.backup(it) }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.restore(it) }
    }

    // Handle pending exports — write to a temp file and share
    LaunchedEffect(state.pendingExportBytes) {
        state.pendingExportBytes?.let { bytes ->
            val ctx = androidx.compose.ui.platform.LocalContext.current
            val file = java.io.File(ctx.cacheDir, state.pendingExportFileName)
            file.writeBytes(bytes)
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = state.pendingExportMime
                val uri = androidx.core.content.FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            ctx.startActivity(android.content.Intent.createChooser(shareIntent, "Share export"))
            vm.consumeExportBytes()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Data", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        Text("Add rounds, import/export, backup/restore. All operations are local — no cloud.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Quick add round", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Tap a number 0..9 to log it as the most recent round.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatChip("Total", state.totalRounds.toString())
            }
            Spacer(Modifier.height(8.dp))
            // Number grid 0..9
            (0..9).chunked(5).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    row.forEach { n ->
                        Button(
                            onClick = { vm.addRound(n) },
                            modifier = Modifier
                                .padding(2.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (n % 2 == 0) com.aicolorpredict.analytics.ui.theme.NumberRed
                                    else com.aicolorpredict.analytics.ui.theme.NumberGreen
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            )
                        ) {
                            Text(n.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Import / Export", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { csvImportLauncher.launch(arrayOf("text/*", "application/vnd.ms-excel", "*/*")) }, modifier = Modifier.weight(1f)) { Text("Import CSV") }
                OutlinedButton(onClick = { jsonImportLauncher.launch(arrayOf("application/json", "*/*")) }, modifier = Modifier.weight(1f)) { Text("Import JSON") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { vm.exportCsv() }, modifier = Modifier.weight(1f)) { Text("Export CSV") }
                OutlinedButton(onClick = { vm.exportJson() }, modifier = Modifier.weight(1f)) { Text("Export JSON") }
            }
        }

        Spacer(Modifier.height(12.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Backup / Restore", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Full backup includes rounds, predictions, and model-performance caches.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { backupLauncher.launch("aicp_backup.json") }, modifier = Modifier.weight(1f)) { Text("Backup") }
                OutlinedButton(onClick = { restoreLauncher.launch(arrayOf("application/json", "*/*")) }, modifier = Modifier.weight(1f)) { Text("Restore") }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { showClearDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Clear All Data")
        }

        state.lastAction?.let {
            Spacer(Modifier.height(8.dp))
            Text("Last action: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text("Error: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear all data?") },
            text = { Text("This permanently deletes every round, prediction, and model-performance record. Cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearAll()
                    showClearDialog = false
                }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } }
        )
    }
}
