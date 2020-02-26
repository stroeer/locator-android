package de.stroeer.locatorandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import de.stroeer.locator_android.Event
import de.stroeer.locator_android.EventType
import de.stroeer.locator_android.Locator
import de.stroeer.locator_android.LocationPermissionRationaleMessage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var locator: Locator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locator = Locator(this)

        btn_get_location.setOnClickListener {
            startLocationDiscoveryProcess()
        }

        btn_get_location_silently.setOnClickListener {
            startSilentLocationDiscoveryProcess()
        }
    }

    private fun startLocationDiscoveryProcess() {
        val permissionRationale = getLocationPermissionRationale()
        locator.getCurrentLocation(permissionRationale) { locationEvent ->
            when (locationEvent) {
                is Event.Location -> handleLocationEvent(locationEvent)
                is Event.Permission -> handlePermissionEvent(locationEvent)
            }
        }
    }

    private fun startSilentLocationDiscoveryProcess() {
        locator.getCurrentLocationSilently { locationEvent ->
            when (locationEvent) {
                is Event.Location -> handleLocationEvent(locationEvent)
                is Event.Permission -> handlePermissionEvent(locationEvent)
            }
        }
    }

    private fun getLocationPermissionRationale() = LocationPermissionRationaleMessage(
        getString(R.string.error_location_disabled_short),
        getString(R.string.error_location_disabled),
        getString(R.string.error_location_disabled_goto_settings),
        getString(R.string.error_location_disabled_cancel)
    )

    private fun handlePermissionEvent(permissionEvent: Event.Permission) {
        when (permissionEvent.event) {
            EventType.LOCATION_PERMISSION_GRANTED -> {
                showToast("Permission granted")
                de.stroeer.locator_android.Logger.logDebug("Permission granted")
            }
            EventType.LOCATION_PERMISSION_NOT_GRANTED -> {
                showToast("Permission NOT granted")
                de.stroeer.locator_android.Logger.logDebug("Permission NOT granted")
            }
            EventType.LOCATION_DISABLED_ON_DEVICE -> {
                showToast("Permission NOT granted permanently")
                de.stroeer.locator_android.Logger.logDebug("Permission NOT granted permanently")
            }
        }
    }

    private fun handleLocationEvent(locationEvent: Event.Location) {
        if (locationEvent.locationData == null) {
            showToast("Location not found")
        } else {
            showToast("${locationEvent.locationData?.latitude} : ${locationEvent.locationData?.longitude}")
        }
    }

    private fun  showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
