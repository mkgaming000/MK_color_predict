package com.aicolorpredict.analytics.ui.settings

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.backup.BackupRestoreManager
import com.aicolorpredict.analytics.data.importer.CsvExporter
import com.aicolorpredict.analytics.data.importer.CsvImporter
import com.aicolorpredict.analytics.data.importer.JsonExporter
import com.aicolorpredict.analytics.data.importer.JsonImporter
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.ui.theme.ThemePrefs
import com.aicolorpredict.analytics.util.AppDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class SettingsUiState(
    val darkMode: Boolean = true,
    val dynamicColor: Boolean = true,
    val totalRounds: Int = 0,
    val lastAction: String? = null,
    val errorMessage: String? = null,
    val pendingExportBytes: ByteArray? = null,
    val pendingExportMime: String = "text/csv",
    val pendingExportFileName: String = "aicp_export.csv",
    val showClearDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val backupRestore: BackupRestoreManager,
    private val dispatchers: AppDispatchers
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SettingsUiState(
        darkMode = ThemePrefs.darkMode.value,
        dynamicColor = ThemePrefs.dynamicColor.value
    ))
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            roundRepo.observeCount().collect { count ->
                _state.value = _state.value.copy(totalRounds = count)
            }
        }
    }

    fun setDarkMode(v: Boolean) {
        ThemePrefs.setDarkMode(v)
        _state.value = _state.value.copy(darkMode = v)
    }

    fun setDynamicColor(v: Boolean) {
        ThemePrefs.setDynamicColor(v)
        _state.value = _state.value.copy(dynamicColor = v)
    }

    fun importCsv(uri: Uri) {
        Log.d("SettingsVM", "Import CSV: $uri")
        viewModelScope.launch(dispatchers.io) {
            try {
                val rounds = getApplication<Application>().contentResolver.openInputStream(uri).use { CsvImporter.parse(it!!) }
                roundRepo.addMany(rounds)
                _state.value = _state.value.copy(lastAction = "Imported ${rounds.size} rounds from CSV")
            } catch (t: Throwable) {
                _state.value = _state.value.copy(errorMessage = "CSV import failed: ${t.message}")
            }
        }
    }

    fun importJson(uri: Uri) {
        Log.d("SettingsVM", "Import JSON: $uri")
        viewModelScope.launch(dispatchers.io) {
            try {
                val rounds = getApplication<Application>().contentResolver.openInputStream(uri).use { JsonImporter.parse(it!!) }
                roundRepo.addMany(rounds)
                _state.value = _state.value.copy(lastAction = "Imported ${rounds.size} rounds from JSON")
            } catch (t: Throwable) {
                _state.value = _state.value.copy(errorMessage = "JSON import failed: ${t.message}")
            }
        }
    }

    fun exportCsv() {
        Log.d("SettingsVM", "Export CSV clicked")
        viewModelScope.launch(dispatchers.io) {
            try {
                val rounds = roundRepo.all()
                val baos = ByteArrayOutputStream()
                CsvExporter.write(rounds, baos)
                _state.value = _state.value.copy(
                    pendingExportBytes = baos.toByteArray(),
                    pendingExportMime = "text/csv",
                    pendingExportFileName = "aicp_rounds.csv",
                    lastAction = "Exported ${rounds.size} rounds to CSV"
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(errorMessage = "CSV export failed: ${t.message}")
            }
        }
    }

    fun exportJson() {
        Log.d("SettingsVM", "Export JSON clicked")
        viewModelScope.launch(dispatchers.io) {
            try {
                val rounds = roundRepo.all()
                val baos = ByteArrayOutputStream()
                JsonExporter.write(rounds, baos)
                _state.value = _state.value.copy(
                    pendingExportBytes = baos.toByteArray(),
                    pendingExportMime = "application/json",
                    pendingExportFileName = "aicp_rounds.json",
                    lastAction = "Exported ${rounds.size} rounds to JSON"
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(errorMessage = "JSON export failed: ${t.message}")
            }
        }
    }

    fun backup(uri: Uri) {
        Log.d("SettingsVM", "Backup to: $uri")
        viewModelScope.launch(dispatchers.io) {
            try {
                getApplication<Application>().contentResolver.openOutputStream(uri).use { backupRestore.export(it!!) }
                _state.value = _state.value.copy(lastAction = "Backup written")
            } catch (t: Throwable) {
                _state.value = _state.value.copy(errorMessage = "Backup failed: ${t.message}")
            }
        }
    }

    fun restore(uri: Uri) {
        Log.d("SettingsVM", "Restore from: $uri")
        viewModelScope.launch(dispatchers.io) {
            try {
                getApplication<Application>().contentResolver.openInputStream(uri).use { backupRestore.restore(it!!) }
                _state.value = _state.value.copy(lastAction = "Backup restored")
            } catch (t: Throwable) {
                _state.value = _state.value.copy(errorMessage = "Restore failed: ${t.message}")
            }
        }
    }

    fun showClearDialog(v: Boolean) {
        _state.value = _state.value.copy(showClearDialog = v)
    }

    fun clearAll() {
        Log.d("SettingsVM", "Clear all data confirmed")
        viewModelScope.launch(dispatchers.io) {
            try {
                roundRepo.clearAll()
                predictionRepo.clearAll()
                _state.value = _state.value.copy(lastAction = "Cleared all data", showClearDialog = false)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(errorMessage = "Clear failed: ${t.message}", showClearDialog = false)
            }
        }
    }

    fun consumeExportBytes(): ByteArray? {
        val bytes = _state.value.pendingExportBytes
        _state.value = _state.value.copy(pendingExportBytes = null)
        return bytes
    }

    fun consumeMessage() {
        _state.value = _state.value.copy(errorMessage = null, lastAction = null)
    }
}
