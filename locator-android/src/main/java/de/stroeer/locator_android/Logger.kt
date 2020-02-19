package de.stroeer.locator_android

import android.util.Log

object Logger {

    var isLoggingEnabled = false

    fun logDebug(message: String?) {
        if (isLoggingEnabled) {
            Log.d("tomo-location", "$message")
        }
    }
}
