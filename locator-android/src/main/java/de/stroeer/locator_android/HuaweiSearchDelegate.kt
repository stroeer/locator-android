package de.stroeer.locator_android

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import com.huawei.hms.location.*

class HuaweiSearchDelegate(val activity: Activity,
                           val eventCallback: (Event) -> Unit,
                           val huaweiFusedLocationClient: FusedLocationProviderClient) {

    private val locationCallback by lazy {
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
                    Logger.logDebug("HuaweiSearchDelegate: onLocationAvailability(${locationAvailability?.isLocationAvailable})")
                    onLocationNotFound()
                }
            }
        }
    }

    fun startSearchForCurrentLocation() {
        if (isLocationDisabled()) {
            return
        }
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10 * 1000)        // 10 seconds, in milliseconds
            .setFastestInterval(1 * 1000)

        huaweiFusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    fun stopSearchForCurrentLocation() {
        huaweiFusedLocationClient.removeLocationUpdates(locationCallback)
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