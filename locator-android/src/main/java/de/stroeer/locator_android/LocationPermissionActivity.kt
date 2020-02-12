package de.stroeer.locator_android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.stroeer.locator_android.LocationProvider.Companion.EXTRA_LOCATION_PERMISSION_RATIONALE
import com.example.tomo_location.Logger

class LocationPermissionActivity  : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private lateinit var permissionRationaleMessage: LocationPermissionRationaleMessage
    private var permissionRequestedTimes = 0

    internal enum class PermissionType { APP_LOCATION_PERMISSION, DEVICE_LOCATION_PERMISSION }

    private val unresolvedPermission = mutableListOf<PermissionType>()
    private val PERMISSION_REQUEST_CODE = 777

    // this is the entry point
    override fun onResume() {
        super.onResume()
        permissionRationaleMessage = intent.extras?.getParcelable(EXTRA_LOCATION_PERMISSION_RATIONALE) ?: LocationPermissionRationaleMessage()

        definePermissionStack()
        resolveNextPermissionInStack()
    }

    private fun resolveNextPermissionInStack() {
        if (unresolvedPermission.size == 0) {
            onPermissionGranted()
        } else {
            when (unresolvedPermission[0]) {
                PermissionType.APP_LOCATION_PERMISSION -> requestAppPermissions(this, permissions)
                PermissionType.DEVICE_LOCATION_PERMISSION -> {
                    if (permissionRequestedTimes == 0) onLocationDisabled() else onLocationStillDisabled()
                }
            }
        }
    }

    private fun definePermissionStack() {
        unresolvedPermission.clear()
        if (!hasAppPermissions(this, permissions)) {
            unresolvedPermission.add(PermissionType.APP_LOCATION_PERMISSION)
        }
        if (isLocationDisabled()) {
            unresolvedPermission.add(PermissionType.DEVICE_LOCATION_PERMISSION)
        }
    }

    private fun isLocationDisabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            !locationManager.isLocationEnabled
        } else {
            return !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }

    private fun onLocationDisabled() {
        Logger.logDebug("onLocationDisabled()")
        AlertDialog.Builder(this)
            .setTitle(permissionRationaleMessage.rationaleTitle)
            .setMessage(permissionRationaleMessage.rationaleMessage)
            .setPositiveButton(permissionRationaleMessage.rationaleYes) { _, _ ->
                permissionRequestedTimes++
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(permissionRationaleMessage.rationaleNo) { _, _ ->
                broadcastPermissionEvent(LocationPermissionEvent.LOCATION_DISABLED)
            }
            .setCancelable(false) // prevents bugs
            .show()
    }

    private fun onLocationStillDisabled() {
        Logger.logDebug("LocationPermissionActivity: onLocationStillDisabled()")
        broadcastPermissionEvent(LocationPermissionEvent.LOCATION_STILL_DISABLED)
    }

    private fun onPermissionGranted() {
        Logger.logDebug("LocationPermissionActivity: onLocationDisabled()")
        broadcastPermissionEvent(LocationPermissionEvent.LOCATION_PERMISSION_GRANTED)
    }

    private fun broadcastPermissionEvent(event: LocationPermissionEvent, finishActivity: Boolean = true) {
        val intent = LocationPermissionBroadcastReceiver.getBroadcastIntent(event)
        sendBroadcast(intent)
        if (finishActivity) {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> finish() // user interaction was cancelled
                grantResults.all { it == PackageManager.PERMISSION_GRANTED } -> { }
                else -> finish() // permissions not granted
            }
        }
    }

    private fun hasAppPermissions(activity: Context?, permissions: Array<String>): Boolean {
        activity?.let {
            return permissions.all { next -> ContextCompat.checkSelfPermission(activity, next) == PackageManager.PERMISSION_GRANTED }
        }
        return false
    }

    private fun requestAppPermissions(activity: Activity, permissions: Array<String>) {
        ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE)
    }

}