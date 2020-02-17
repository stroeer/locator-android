package de.stroeer.locator_android

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat

enum class InternalPermissionType { APP_LOCATION_PERMISSION, DEVICE_LOCATION_PERMISSION }

class LocationPermissionHelper(private val permissions: Array<String>) {

    fun unresolvedPermissions(context: Context?): List<InternalPermissionType> {
        val permissionStack = mutableListOf<InternalPermissionType>()
        if (!hasAppPermissions(context, permissions)) {
            permissionStack.add(InternalPermissionType.APP_LOCATION_PERMISSION)
        }
        if (isLocationDisabled(context)) {
            permissionStack.add(InternalPermissionType.DEVICE_LOCATION_PERMISSION)
        }
        return permissionStack
    }

    private fun hasAppPermissions(context: Context?, permissions: Array<String>): Boolean {
        context?.let {
            return permissions.all { next -> ContextCompat.checkSelfPermission(context, next) == PackageManager.PERMISSION_GRANTED }
        }
        return false
    }

    private fun isLocationDisabled(context: Context?): Boolean {
        if (context == null) return true
        val locationManager: LocationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            !locationManager.isLocationEnabled
        } else {
            return !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }

}