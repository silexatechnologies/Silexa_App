package com.silexa.crm.call

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CallUtils {

    fun formatTime(timestamp: Long): String {

        val formatter = SimpleDateFormat(
            "dd-MM-yyyy hh:mm:ss a",
            Locale.getDefault()
        )

        return formatter.format(Date(timestamp))
    }

    fun formatDuration(durationMillis: Long): String {

        val totalSeconds =
            durationMillis / 1000

        val hours =
            totalSeconds / 3600

        val minutes =
            (totalSeconds % 3600) / 60

        val seconds =
            totalSeconds % 60

        return String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            hours,
            minutes,
            seconds
        )
    }
}