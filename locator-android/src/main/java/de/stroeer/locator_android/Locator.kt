package de.stroeer.locator_android

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.lang.Exception

sealed class Event {
    data class Location(val locationData: android.location.Location?) : Event()
    data class Permission(val event: EventType, val exception: Exception? = null) : Event()
}

enum class EventType {
    LOCATION_PERMISSION_GRANTED,
    LOCATION_PERMISSION_NOT_GRANTED,
    LOCATION_DISABLED_ON_DEVICE
}

class Locator(val activity: Context) {

    private lateinit var locationProvider: LocationProvider

    fun getCurrentLocation(rationale: LocationPermissionRationaleMessage? = null,
                           eventCallback: (Event) -> Unit
    ) {
        initLocationProvider(activity, eventCallback, rationale)
        locationProvider.startLocationDiscoveryOrStartPermissionResolution()
    }

    fun getCurrentLocationSilently(eventCallback: (Event) -> Unit) {
        initLocationProvider(activity, eventCallback, null)
        locationProvider.startSilentLocationDiscovery()
    }

    private fun initLocationProvider(activity: Context,
                                     eventCallback: (Event) -> Unit,
                                     locationPermissionRationaleMessage: LocationPermissionRationaleMessage?
    ) {
        if (!::locationProvider.isInitialized) {
            val locationProviderType = LocationDelegate.GOOGLE
            locationProvider = LocationProvider(activity, eventCallback, locationProviderType, locationPermissionRationaleMessage)
        }
    }

    private fun isGooglePlayServicesAvailable(context: Context) =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

    fun stopService() {
        if (::locationProvider.isInitialized) {
            locationProvider.stopService()
        }
    }
}


