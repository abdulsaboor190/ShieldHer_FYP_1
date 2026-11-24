package com.shieldher.dashboard.evidence

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldher.dashboard.data.ImportOutcome
import com.shieldher.dashboard.data.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** All states the Import screen can be in. */
sealed interface ImportScreenState {
    /** No import in progress; initial state or after a cancelled picker. */
    data object Idle : ImportScreenState

    /** Import is in progress; buttons should be disabled. */
    data object Saving : ImportScreenState

    /** Import succeeded. [id] is the new evidence id. */
    data class Saved(val id: String) : ImportScreenState

    /** The chosen file was too large. */
    data object TooLarge : ImportScreenState

    /** The chosen file type is not supported. */
    data object UnsupportedType : ImportScreenState

    /** Import failed for an unknown reason; nothing was saved. */
    data object Failed : ImportScreenState
}

class ImportViewModel(
    private val gateway: com.shieldher.dashboard.data.EvidenceGateway =
        ServiceLocator.evidenceGateway
) : ViewModel() {

    private val _state = MutableStateFlow<ImportScreenState>(ImportScreenState.Idle)
    val state: StateFlow<ImportScreenState> = _state.asStateFlow()

    /**
     * Called when the system document picker returns a URI.
     * [uri] must NOT be logged or stored beyond this call.
     * [mimeType] is retrieved by the Fragment via ContentResolver before calling here;
     * it is NOT displayed or logged.
     */
    fun onUriSelected(uri: Uri, mimeType: String?) {
        if (_state.value == ImportScreenState.Saving) return
        viewModelScope.launch {
            _state.value = ImportScreenState.Saving
            val outcome = try {
                gateway.importFromUri(uri, mimeType)
            } catch (_: Exception) {
                ImportOutcome.Failed
            }
            _state.value = when (outcome) {
                is ImportOutcome.Success -> ImportScreenState.Saved(outcome.id)
                ImportOutcome.TooLarge -> ImportScreenState.TooLarge
                ImportOutcome.Unsupported -> ImportScreenState.UnsupportedType
                ImportOutcome.Failed -> ImportScreenState.Failed
            }
        }
    }

    /** Called when the picker is cancelled (result URI is null). Returns to Idle silently. */
    fun onPickerCancelled() {
        // Only reset to Idle if we were actually waiting; never interrupt a Saving state.
        if (_state.value == ImportScreenState.Idle) return
        if (_state.value !is ImportScreenState.Saving) {
            _state.value = ImportScreenState.Idle
        }
    }

    /** Resets to Idle so the user can import another file after a success or error. */
    fun resetToIdle() {
        _state.value = ImportScreenState.Idle
    }
}
