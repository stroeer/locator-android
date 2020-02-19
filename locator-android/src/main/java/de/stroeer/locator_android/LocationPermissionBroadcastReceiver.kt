package de.stroeer.locator_android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class LocationPermissionBroadcastReceiver(private val locationPermissionCallback: ((LocationPermissionEvent) -> Unit)?)
    : BroadcastReceiver() {

    companion object {
        const val LOCATION_PERMISSION_ACTION = "com.tomo-location.CUSTOM_INTENT"
        private const val LOCATION_PERMISSION_EXTRA = "locationPermissionEvent"

        fun getBroadcastIntent(isPermissionGranted: LocationPermissionEvent): Intent {
            return Intent().apply {
                action = LOCATION_PERMISSION_ACTION
                addCategory(Intent.CATEGORY_DEFAULT)
                putExtra(LOCATION_PERMISSION_EXTRA, isPermissionGranted)
            }
        }

        fun getBroadcastIntentFilter(): IntentFilter? {
            return IntentFilter().apply {
                addAction(LOCATION_PERMISSION_ACTION)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
        }

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        if (intent.action == LOCATION_PERMISSION_ACTION) {
            Logger.logDebug("LocationPermissionBroadcastReceiver: onReceive()")
            val locationPermissionEvent = intent.getSerializableExtra(LOCATION_PERMISSION_EXTRA) as LocationPermissionEvent
            locationPermissionCallback?.invoke( locationPermissionEvent )
            context?.unregisterReceiver(this)
        }
    }
}