# Getting location on Android

This lib is an easy way to get the location inside Android applications. The new thing about it, it either uses Google Play Services (if installed) or Huawei Mobile Services.

## Integration steps

On app module level, edit build.gradle:

```
implementation 'com.github.stroeer:locator-android:0.0.1'
```

Define text for getting permissions:
```kotlin
    LocationPermissionRationaleMessage(
        getString(R.string.error_location_disabled_short),
        getString(R.string.error_location_disabled),
        getString(R.string.error_location_disabled_goto_settings),
        getString(R.string.error_location_disabled_cancel)
    )
```

Inside your Kotlin file:

```kotlin
    LocationHelper.getCurrentLocation(this, permissionRationale) { locationEvent ->
        when (locationEvent) {
            is Event.Location -> handleLocationEvent(locationEvent)
            is Event.Permission -> handlePermissionEvent(locationEvent)
        }
    }
```

Handling events:

```kotlin
private fun handleLocationEvent(locationEvent: Event.Location) {
        val location = locationEvent.locationData
        if (location == null) {
            onLocationNotFound()
         } else {
            processLatLong(location.latitude, location.longitude)
         }
     }
```

```kotlin
private fun handlePermissionEvent(permissionEvent: Event.Permission) {
        when (permissionEvent.event) {
            EventType.LOCATION_PERMISSION_GRANTED -> {
                floating_search_view.showProgress()
            }

            EventType.LOCATION_PERMISSION_NOT_GRANTED -> {
               onLocationDisabled()
            }

           EventType.LOCATION_PERMISSION_NOT_GRANTED_PERMANENTLY -> {
                onLocationStillDisabled()
            }
        }
    }
```