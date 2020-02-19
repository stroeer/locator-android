package de.stroeer.locator_android

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*

class GoogleSearchDelegate(val activity: Activity,
                           val eventCallback: (Event) -> Unit,
                           val googleFusedLocationClient: FusedLocationProviderClient) {

    val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.lastLocation?.let { location ->
                    eventCallback(Event.Location(location))
                    stopSearchForCurrentLocation()
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                if (locationAvailability == null || !locationAvailability.isLocationAvailable) {
                    Logger.logDebug("GoogleSearchDelegate: onLocationAvailability(${locationAvailability?.isLocationAvailable})")
                    onLocationNotFound()
                }
            }
        }
    }

    fun startSearchForCurrentLocation() {
        if (isLocationDisabled()) {
            return
        }
        googleApiClient.connect()
    }

    fun stopSearchForCurrentLocation() {
        googleApiClient.unregisterConnectionCallbacks(googleApiClientConnectionCallback)
        googleApiClient.disconnect()
        googleFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private val googleApiClientConnectionCallback by lazy {
        object : GoogleApiClient.ConnectionCallbacks {
            override fun onConnected(var1: Bundle?) {
                val locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000)

                googleFusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper())
            }

            override fun onConnectionSuspended(cause: Int) {
                Logger.logDebug("GoogleSearchDelegate: onConnectionSuspended($cause)")
            }
        }
    }

    private val googleApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(activity)
            .addConnectionCallbacks(googleApiClientConnectionCallback)
            .addOnConnectionFailedListener { connectionResult ->
                Logger.logDebug("GoogleSearchDelegate: Connection failed: $connectionResult")
                onLocationNotFound()
            }
            .addApi(LocationServices.API)
            .build()
    }

    private fun isLocationDisabled(): Boolean {
        val locationService = activity.getSystemService(Context.LOCATION_SERVICE) ?: return true
        val locationManager = locationService as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            !locationManager.isLocationEnabled
        } else {
            return !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }

    private fun onLocationNotFound() {
        eventCallback(Event.Location(null))
    }

}