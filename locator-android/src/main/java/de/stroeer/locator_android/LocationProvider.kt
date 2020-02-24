package de.stroeer.locator_android

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.FusedLocationProviderClient as GoogleFLPC
import com.google.android.gms.location.LocationServices as GoogleLocationServices
import com.huawei.hms.location.FusedLocationProviderClient as HuaweiFLPC
import com.huawei.hms.location.LocationServices as HuaweiLocationServices

enum class LocationDelegate { GOOGLE, HUAWEI }

class LocationProvider(
    val activity: Activity,
    val eventCallback: (Event) -> Unit,
    private val locationDelegateType: LocationDelegate,
    private val locationPermissionRationaleMessage: LocationPermissionRationaleMessage?
) {

    companion object {
        const val EXTRA_LOCATION_PERMISSION_RATIONALE = "EXTRA_LOCATION_PERMISSION_RATIONALE"
        const val EXTRA_LOCATION_PERMISSION_RESOLUTION_NECESSARY = "EXTRA_LOCATION_PERMISSION_RESOLUTION_NECESSARY"
    }

    private val googleFusedLocationClient: GoogleFLPC by lazy {
        GoogleLocationServices.getFusedLocationProviderClient(activity)
    }
    private val huaweiFusedLocationClient: HuaweiFLPC by lazy {
        HuaweiLocationServices.getFusedLocationProviderClient(activity)
    }
    private val googleLocationSearchDelegate: GoogleSearchDelegate by lazy {
        GoogleSearchDelegate(activity, eventCallback, googleFusedLocationClient)
    }
    private val huaweiLocationSearchDelegate: HuaweiSearchDelegate by lazy {
        HuaweiSearchDelegate(activity, eventCallback, huaweiFusedLocationClient)
    }
    private val filter by lazy {
        LocationPermissionBroadcastReceiver.getBroadcastIntentFilter()
    }
    private val locationPermissionBroadcastReceiver by lazy {
        LocationPermissionBroadcastReceiver(locationPermissionCallbacks)
    }

    private val locationPermissionCallbacks: ((LocationPermissionEvent) -> Unit)? = {
        when (it) {
            LocationPermissionEvent.LOCATION_PERMISSION_GRANTED -> {
                getLastLocation()
                eventCallback(Event.Permission(EventType.LOCATION_PERMISSION_GRANTED))
            }
            LocationPermissionEvent.LOCATION_DISABLED -> {
                eventCallback(Event.Permission(EventType.LOCATION_PERMISSION_NOT_GRANTED))
            }
            LocationPermissionEvent.LOCATION_STILL_DISABLED -> {
                eventCallback(Event.Permission(EventType.LOCATION_DISABLED_ON_DEVICE))
            }
        }
    }

    fun stopService() {
        // stop searching
        when (locationDelegateType) {
            LocationDelegate.GOOGLE -> googleLocationSearchDelegate.stopSearchForCurrentLocation()
            LocationDelegate.HUAWEI -> huaweiLocationSearchDelegate.stopSearchForCurrentLocation()
        }
    }

    /**
     *  Start permission resolution process if necessary and then try to find location
     */
    fun startLocationDiscoveryOrStartPermissionResolution() {
        if (getUnresolvedPermissions().isEmpty()) {
            getLastLocation()
        } else {
            activity.registerReceiver(locationPermissionBroadcastReceiver, filter)
            startPermissionAndResolutionProcess(activity)
        }
    }

    /**
     *  Try to find location silently; throw error if permissions not granted
     */
    fun startSilentLocationDiscovery() {
        if (getUnresolvedPermissions().isEmpty()) {
            getLastLocation()
        } else {
            eventCallback(Event.Permission(EventType.LOCATION_PERMISSION_NOT_GRANTED))
        }
    }

    private fun getUnresolvedPermissions(): List<InternalPermissionType> {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        return LocationPermissionHelper(permissions).unresolvedPermissions(activity)
    }

    private fun getLastLocation() {
        when (locationDelegateType) {
            LocationDelegate.GOOGLE -> getLastLocationGoogle()
            LocationDelegate.HUAWEI -> getLastLocationHuawei()
        }
    }

    private fun startPermissionAndResolutionProcess(context: Context) {
        if (isAppInForeground(context)) {
            val intent = Intent(context, LocationPermissionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(EXTRA_LOCATION_PERMISSION_RATIONALE, locationPermissionRationaleMessage)
            }
            context.applicationContext.startActivity(intent)
        }
    }

    // source: https://stackoverflow.com/questions/43378841/check-if-app-is-running-in-foreground-or-background-with-sync-adapter
    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        return runningAppProcesses.any { it.processName == context.packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }

    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     *  Google location service
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    private fun getLastLocationGoogle() {
        googleFusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // GPS location can be null if GPS is switched off
                    eventCallback(Event.Location(location))
                    stopService()
                } else {
                    // fallback to active location discovery
                    Logger.logDebug("LocationProvider (Google): Last known location is null")
                    googleLocationSearchDelegate.startSearchForCurrentLocation()
                }
            }
            .addOnFailureListener { e ->
                Logger.logDebug("LocationProvider: ${e.message}")
                googleLocationSearchDelegate.startSearchForCurrentLocation()
            }
    }

    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     *  Huawei location service
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    private fun getLastLocationHuawei() {
        huaweiFusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // GPS location can be null if GPS is switched off
                    eventCallback(Event.Location(location))
                    stopService()
                } else {
                    // fallback to active location discovery
                    Logger.logDebug("LocationProvider (Huawei): Last known location is null")
                    huaweiLocationSearchDelegate.startSearchForCurrentLocation()
                }
            }
            .addOnFailureListener { e ->
                Logger.logDebug("LocationProvider: ${e.message}")
                huaweiLocationSearchDelegate.startSearchForCurrentLocation()
            }
    }
}