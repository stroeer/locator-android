package com.example.tomo_location

import android.util.Log

object Logger {

    var isLoggingEnabled = false

    fun logDebug(message: String?) {
        if (isLoggingEnabled) {
            Log.d("tomo-location", "$message")
        }
    }
}
