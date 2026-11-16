package com.shieldher.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * FE-0.2: Proves kotlinx-coroutines-test and arch-core-testing work in unit tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SpikeToolchainTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private class TrivialLiveDataHolder {
        private val _data = MutableLiveData<String>()
        val data: LiveData<String> = _data

        fun post(value: String) {
            _data.value = value
        }
    }

    @Test
    fun coroutinesTestAndInstantTaskExecutorRuleWork() = runTest {
        val holder = TrivialLiveDataHolder()
        holder.post("shieldher_spike_passed")
        assertEquals("shieldher_spike_passed", holder.data.value)
    }
}
