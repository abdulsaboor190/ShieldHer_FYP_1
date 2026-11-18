package com.shieldher.dashboard.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldher.R
import com.shieldher.dashboard.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Interface for a fake data loader used by [TemplateViewModel].
 */
interface TemplateDataSource {
    suspend fun loadItems(): List<String>
}

/**
 * Minimal ViewModel demonstrating the ShieldHer UiState pattern.
 */
class TemplateViewModel(
    private val dataSource: TemplateDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<String>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<String>>> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun retry() {
        loadData()
    }

    private fun loadData() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val items = dataSource.loadItems()
                _uiState.value = if (items.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Content(items)
                }
            } catch (_: Exception) {
                _uiState.value = UiState.Error(
                    messageRes = R.string.error_generic,
                    canRetry = true
                )
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class UiStateTemplateTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_startsInLoading() = runTest {
        val fakeSource = object : TemplateDataSource {
            override suspend fun loadItems(): List<String> = listOf("example item 1")
        }

        val viewModel = TemplateViewModel(fakeSource)

        // Before advancing dispatcher, state is Loading
        assertEquals(UiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun uiState_nonEmptyListProducesContent() = runTest {
        val fakeItems = listOf("example item 1", "example item 2")
        val fakeSource = object : TemplateDataSource {
            override suspend fun loadItems(): List<String> = fakeItems
        }

        val viewModel = TemplateViewModel(fakeSource)
        advanceUntilIdle()

        assertEquals(UiState.Content(fakeItems), viewModel.uiState.value)
    }

    @Test
    fun uiState_emptyListProducesEmpty() = runTest {
        val fakeSource = object : TemplateDataSource {
            override suspend fun loadItems(): List<String> = emptyList()
        }

        val viewModel = TemplateViewModel(fakeSource)
        advanceUntilIdle()

        assertEquals(UiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun uiState_failingSourceProducesErrorWithCanRetryTrue() = runTest {
        val fakeSource = object : TemplateDataSource {
            override suspend fun loadItems(): List<String> = throw RuntimeException("Network error")
        }

        val viewModel = TemplateViewModel(fakeSource)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        val errorState = state as UiState.Error
        assertEquals(R.string.error_generic, errorState.messageRes)
        assertTrue(errorState.canRetry)
    }

    @Test
    fun uiState_retryAfterFailureReachesContent() = runTest {
        var shouldFail = true
        val fakeItems = listOf("example item 1")
        val fakeSource = object : TemplateDataSource {
            override suspend fun loadItems(): List<String> {
                if (shouldFail) throw RuntimeException("Network error")
                return fakeItems
            }
        }

        val viewModel = TemplateViewModel(fakeSource)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Error)

        // Switch to succeeding and trigger retry
        shouldFail = false
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(UiState.Content(fakeItems), viewModel.uiState.value)
    }
}
