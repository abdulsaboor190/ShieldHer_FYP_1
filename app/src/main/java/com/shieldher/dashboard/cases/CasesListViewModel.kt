package com.shieldher.dashboard.cases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldher.R
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.data.Case
import com.shieldher.dashboard.data.CaseRepository
import com.shieldher.dashboard.data.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CasesListViewModel(
    private val repository: CaseRepository = ServiceLocator.caseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Case>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Case>>> = _uiState.asStateFlow()

    init {
        loadCases()
    }

    fun loadCases() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.observeCases()
                .catch { ex ->
                    if (ex is CaseStoreException) {
                        _uiState.value = UiState.Error(
                            messageRes = R.string.cases_list_unreadable_store,
                            canRetry = false
                        )
                    } else {
                        _uiState.value = UiState.Error(
                            messageRes = R.string.cases_list_error_message,
                            canRetry = true
                        )
                    }
                }
                .collect { cases ->
                    if (cases.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        // Newest first
                        val sorted = cases.sortedByDescending { it.createdAtMillis }
                        _uiState.value = UiState.Content(sorted)
                    }
                }
        }
    }

    fun createCase(title: String, description: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return
        val trimmedDesc = description.trim()
        viewModelScope.launch {
            try {
                repository.createCase(trimmedTitle, trimmedDesc)
            } catch (ex: CaseStoreException) {
                _uiState.value = UiState.Error(
                    messageRes = R.string.cases_list_unreadable_store,
                    canRetry = false
                )
            } catch (_: Exception) {
                // If creation fails, observeCases will keep previous state
            }
        }
    }

    fun renameCase(caseId: String, newTitle: String) {
        val trimmedTitle = newTitle.trim()
        if (trimmedTitle.isBlank()) return
        viewModelScope.launch {
            try {
                repository.renameCase(caseId, trimmedTitle)
            } catch (ex: CaseStoreException) {
                _uiState.value = UiState.Error(
                    messageRes = R.string.cases_list_unreadable_store,
                    canRetry = false
                )
            } catch (_: Exception) {
            }
        }
    }

    fun deleteCase(caseId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCase(caseId)
            } catch (ex: CaseStoreException) {
                _uiState.value = UiState.Error(
                    messageRes = R.string.cases_list_unreadable_store,
                    canRetry = false
                )
            } catch (_: Exception) {
            }
        }
    }
}
