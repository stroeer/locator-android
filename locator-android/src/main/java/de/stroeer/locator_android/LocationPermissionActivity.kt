package de.stroeer.locator_android

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import de.stroeer.locator_android.LocationProvider.Companion.EXTRA_LOCATION_PERMISSION_RATIONALE

class LocationPermissionActivity  : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private lateinit var permissionRationaleMessage: LocationPermissionRationaleMessage
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private var permissionRequestedTimes = 0

    private val PERMISSION_REQUEST_CODE = 777

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionHelper = LocationPermissionHelper(permissions)
    }

    // this is the entry point
    override fun onResume() {
        super.onResume()
        permissionRationaleMessage = intent.extras?.getParcelable(EXTRA_LOCATION_PERMISSION_RATIONALE) ?: LocationPermissionRationaleMessage()

        val unresolvedPermissions = locationPermissionHelper.unresolvedPermissions(this)
        resolveNextPermissionInStack(unresolvedPermissions)
    }

    private fun resolveNextPermissionInStack(unresolvedPermissions: List<InternalPermissionType>) {
        if (unresolvedPermissions.isEmpty()) {
            onPermissionGranted()
        } else {
            when (unresolvedPermissions[0]) {
                InternalPermissionType.APP_LOCATION_PERMISSION -> requestAppPermissions(this, permissions)
                InternalPermissionType.DEVICE_LOCATION_PERMISSION -> {
                    if (permissionRequestedTimes == 0) onLocationDisabled() else onLocationStillDisabled()
                }
            }
        }
    }

    private fun onLocationDisabled() {
        Logger.logDebug("LocationPermissionActivity: onLocationDisabled()")
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
        Logger.logDebug("LocationPermissionActivity: onPermissionGranted()")
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



    private fun requestAppPermissions(activity: Activity, permissions: Array<String>) {
        ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE)
    }

}