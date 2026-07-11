package com.aicolorpredict.analytics.ui.enter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicolorpredict.analytics.domain.model.colorsForNumber
import com.aicolorpredict.analytics.ui.components.ColorSwatchRow
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.ui.components.NumberButton
import com.aicolorpredict.analytics.ui.components.numberColor
import com.aicolorpredict.analytics.ui.theme.NumberGreen
import com.aicolorpredict.analytics.ui.theme.NumberRed
import com.aicolorpredict.analytics.ui.theme.NumberViolet
import com.aicolorpredict.analytics.util.DateUtils
import java.util.Calendar

@Composable
fun EnterScreen(
    vm: EnterViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on save success
    LaunchedEffect(state.saved) {
        if (state.saved) {
            snackbarHostState.showSnackbar("Round saved — prediction updated")
            vm.resetSaved()
        }
    }
    // Show error
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Enter Result", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Log a round to retrain the models.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Date & Time pickers
            GlassCard {
                Text("When", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = state.epochMs }
                            DatePickerDialog(context, { _, y, m, d ->
                                val newCal = Calendar.getInstance().apply {
                                    timeInMillis = state.epochMs
                                    set(y, m, d)
                                }
                                vm.setEpochMs(newCal.timeInMillis)
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(DateUtils.displayShort(state.epochMs).take(6))
                    }
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = state.epochMs }
                            TimePickerDialog(context, { _, h, m ->
                                val newCal = Calendar.getInstance().apply {
                                    timeInMillis = state.epochMs
                                    set(Calendar.HOUR_OF_DAY, h)
                                    set(Calendar.MINUTE, m)
                                }
                                vm.setEpochMs(newCal.timeInMillis)
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(state.epochMs.let { DateUtils.displayShort(it).drop(7) })
                    }
                }
            }

            // Round number (optional)
            GlassCard {
                Text("Round Number (optional)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.roundNumber,
                    onValueChange = vm::setRoundNumber,
                    placeholder = { Text("e.g. 12345") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Number buttons 0..9
            GlassCard {
                Text("Number", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                // 2 rows × 5 columns
                (0..9).chunked(5).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { n ->
                            NumberButton(
                                number = n,
                                selected = state.selectedNumber == n,
                                onClick = { vm.selectNumber(n) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Color preview
                state.selectedNumber?.let { n ->
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Colors: ", style = MaterialTheme.typography.bodyMedium)
                        ColorSwatchRow(number = n, swatchSize = 18.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            colorsForNumber(n).joinToString(" + ") { it.display },
                            style = MaterialTheme.typography.bodyMedium,
                            color = numberColor(n),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Save / Cancel
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { vm.cancel() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cancel")
                }
                Button(
                    onClick = { vm.save() },
                    enabled = state.selectedNumber != null && !state.isSaving,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Save")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
