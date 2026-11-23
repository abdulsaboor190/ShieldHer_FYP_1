package com.shieldher.dashboard.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeFormat {

    fun formatDateTime(millis: Long, zoneId: String?): String {
        val sdf = SimpleDateFormat("d MMM yyyy, HH:mm z", Locale.getDefault())
        val tz = if (zoneId != null) TimeZone.getTimeZone(zoneId) else TimeZone.getDefault()
        sdf.timeZone = tz
        return sdf.format(Date(millis))
    }

    fun formatDateOnlyUtc(millis: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(millis))
    }
}
