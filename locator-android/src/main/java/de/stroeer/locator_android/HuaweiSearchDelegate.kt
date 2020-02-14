package de.stroeer.locator_android

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat.getSystemService
import com.example.tomo_location.Logger
import com.google.android.gms.common.api.GoogleApiClient
import com.huawei.hms.location.*

class HuaweiSearchDelegate(val activity: Activity,
                           val eventCallback: (Event) -> Unit,
                           val huaweiFusedLocationClient: FusedLocationProviderClient) {

    private val REQUEST_CHECK_SETTINGS = 0x1

    val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.lastLocation?.let { location ->
                    eventCallback(Event.Location(location))
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                if (locationAvailability == null || !locationAvailability.isLocationAvailable) {
                    Logger.logDebug("onLocationAvailability(${locationAvailability?.isLocationAvailable})")
                    onLocationNotFound()
                }
            }
        }
    }

    fun startSearchForCurrentLocation() {
        if (isLocationDisabled()) {
            return
        }
        huaweiApiClient.connect()
    }

    fun stopSearchForCurrentLocation() {
        huaweiApiClient.unregisterConnectionCallbacks(huaweiApiClientConnectionCallback)
        huaweiApiClient.disconnect()
        huaweiFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private val huaweiApiClientConnectionCallback by lazy {
        object : com.huawei.hms.support.api.client.ResultCallback<LocationSettingsResult>, GoogleApiClient.ConnectionCallbacks {

            override fun onConnected(var1: Bundle?) {
                val locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000)

                huaweiFusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper())
            }

            override fun onConnectionSuspended(cause: Int) {
                Logger.logDebug("LocationModule.onConnectionSuspended($cause)")
            }

            override fun onResult(locationSettingsResult: LocationSettingsResult) {
                val status = locationSettingsResult.status
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> {/* Location settings are satisfied. startListeningOnLocationUpdates() */ }
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        /* Location settings not satisfied. Show the user a dialog to upgrade location settings. */
                        try {
                            status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                        } catch (e: IntentSender.SendIntentException) {
                            Logger.logDebug("PendingIntent unable to execute request.")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> { /* Location settings are inadequate, and cannot be fixed here. */ }
                }
            }
        }
    }

    private val huaweiApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(activity)
            .addConnectionCallbacks(huaweiApiClientConnectionCallback)
            .addOnConnectionFailedListener { connectionResult ->
                Logger.logDebug("Connection failed: $connectionResult")
                onLocationNotFound()
            }
            .addApi(com.google.android.gms.location.LocationServices.API)
            .build()
    }

    private fun isLocationDisabled(): Boolean {
        val locationService = activity.getSystemService(Context.LOCATION_SERVICE) ?: return false
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