package com.aicolorpredict.analytics.ui.data

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.backup.BackupRestoreManager
import com.aicolorpredict.analytics.data.importer.CsvExporter
import com.aicolorpredict.analytics.data.importer.CsvImporter
import com.aicolorpredict.analytics.data.importer.JsonExporter
import com.aicolorpredict.analytics.data.importer.JsonImporter
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.usecase.AddRoundUseCase
import com.aicolorpredict.analytics.domain.usecase.UpdateModelPerformanceUseCase
import com.aicolorpredict.analytics.util.AppDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class DataUiState(
    val isLoading: Boolean = false,
    val totalRounds: Int = 0,
    val lastAction: String? = null,
    val errorMessage: String? = null,
    val pendingExportBytes: ByteArray? = null,
    val pendingExportMime: String = "text/csv",
    val pendingExportFileName: String = "aicp_export.csv"
)

@HiltViewModel
class DataViewModel @Inject constructor(
    application: Application,
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val addRoundUseCase: AddRoundUseCase,
    private val updatePerformanceUseCase: UpdateModelPerformanceUseCase,
    private val backupRestore: BackupRestoreManager,
    private val dispatchers: AppDispatchers
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(DataUiState())
    val state: StateFlow<DataUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            roundRepo.observeCount().collect { count ->
                _state.value = _state.value.copy(totalRounds = count)
            }
        }
    }

    fun addRound(number: Int) {
        viewModelScope.launch {
            try {
                addRoundUseCase(number)
                // Refresh model performance metrics since the previous round's
                // predictions were just resolved.
                updatePerformanceUseCase()
                _state.value = _state.value.copy(lastAction = "Added round with number $number")
            } catch (t: Throwable) {
                _state.value = _state.value.copy(errorMessage = t.message)
            }
        }
    }

    fun importCsv(uri: Uri) {
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
        viewModelScope.launch(dispatchers.io) {
            try {
                getApplication<Application>().contentResolver.openOutputStream(uri).use { backupRestore.export(it!!) }
                _state.value = _state.value.copy(lastAction = "Backup written to ${uri.lastPathSegment}")
            } catch (t: Throwable) {
                _state.value = _state.value.copy(errorMessage = "Backup failed: ${t.message}")
            }
        }
    }

    fun restore(uri: Uri) {
        viewModelScope.launch(dispatchers.io) {
            try {
                getApplication<Application>().contentResolver.openInputStream(uri).use { backupRestore.restore(it!!) }
                _state.value = _state.value.copy(lastAction = "Restored backup from ${uri.lastPathSegment}")
            } catch (t: Throwable) {
                _state.value = _state.value.copy(errorMessage = "Restore failed: ${t.message}")
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch(dispatchers.io) {
            try {
                roundRepo.clearAll()
                predictionRepo.clearAll()
                _state.value = _state.value.copy(lastAction = "Cleared all rounds, predictions, and model performance")
            } catch (t: Throwable) {
                _state.value = _state.value.copy(errorMessage = "Clear failed: ${t.message}")
            }
        }
    }

    fun consumeExportBytes(): ByteArray? {
        val bytes = _state.value.pendingExportBytes
        _state.value = _state.value.copy(pendingExportBytes = null)
        return bytes
    }

    fun clearMessage() {
        _state.value = _state.value.copy(errorMessage = null, lastAction = null)
    }
}
