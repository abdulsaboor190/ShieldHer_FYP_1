package com.shieldher.dashboard.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class TimeFormatTest {

    @Test
    fun formatDateTime_withFixedUtcZone_formatsCorrectly() {
        val originalDefault = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            // 1700000000000L = Wed, 14 Nov 2023 22:13:20 GMT
            val millis = 1700000000000L
            val formatted = TimeFormat.formatDateTime(millis, "UTC")
            assertEquals("14 Nov 2023, 22:13 UTC", formatted)
        } finally {
            Locale.setDefault(originalDefault)
        }
    }

    @Test
    fun formatDateOnlyUtc_withUtcMidnight_rendersSameCalendarDay() {
        val originalDefault = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            // 1700006400000L = Wed, 15 Nov 2023 00:00:00 GMT (UTC Midnight)
            val millis = 1700006400000L
            val formatted = TimeFormat.formatDateOnlyUtc(millis)
            assertEquals("15 Nov 2023", formatted)
        } finally {
            Locale.setDefault(originalDefault)
        }
    }
}
