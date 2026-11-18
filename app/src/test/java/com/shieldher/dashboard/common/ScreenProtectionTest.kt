package com.shieldher.dashboard.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenProtectionTest {

    @Test
    fun shouldSecure_settingEnabledAndDemoInactive_returnsTrue() {
        assertTrue(shouldSecure(settingEnabled = true, demoModeActive = false))
    }

    @Test
    fun shouldSecure_settingEnabledAndDemoActive_returnsFalse() {
        assertFalse(shouldSecure(settingEnabled = true, demoModeActive = true))
    }

    @Test
    fun shouldSecure_settingDisabledAndDemoInactive_returnsFalse() {
        assertFalse(shouldSecure(settingEnabled = false, demoModeActive = false))
    }

    @Test
    fun shouldSecure_settingDisabledAndDemoActive_returnsFalse() {
        assertFalse(shouldSecure(settingEnabled = false, demoModeActive = true))
    }
}
