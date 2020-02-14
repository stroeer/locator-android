# Getting location on Android

This lib is an easy way to get the location inside Android applications. The new thing about it, it either uses Google Play Services (if installed) or Huawei Mobile Services (https://developer.huawei.com/consumer/en/service/hms/locationservice.html).

## Location use cases

Handling locations is complex. This lib handles all known use cases on Android devices, when trying to get the location.

### S1 - success szenario

Given: No location permission granted

Permission dialog becomes visible.

User grants permission.

Location search starts and a progress indicator appears.

The current location __is__ found.

The location can be used inside application.

### S2 - location not found

Given: No location permission granted

Permission dialog becomes visible.

User grants permission.

Location search starts and a progress indicator appears.

The current location __cannot__ be found.

The location cannot be used inside application.

### S3 - location permission denied

Given: No location permission granted

Permission dialog becomes visible.

User denies permission.

The location cannot be used inside application.

### S4 - location authorisation is later revoked

Given: Location permission granted before, but was later revoked via app settings

Permission dialog becomes visible again.

Next: either S1, S2, or S3

### S5 - location has been deactivated throughout the OS

Given: Location detection is disabled on device.

Message becomes visible, that location detection is disabled and a link to OS settings can be clicked.

The location cannot be used inside application.

## Integration steps

### Development preparation

1. Go to allprojects > repositories and buildscript > repositories, and configure the Maven repository address for HMS SDK.

```gradle
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        maven {url 'http://developer.huawei.com/repo/'}
    }
}

allprojects {
    repositories {
        maven { url 'http://developer.huawei.com/repo/' }
    }
}
```

2. On app module level, add compile dependencies inside file build.gradle:

```gradle
implementation 'com.github.stroeer:locator-android:0.0.1'
```

3. Re-open the modified build.gradle file. You will find a Sync Now link in the upper right corner of the page. Click Sync Now and wait until synchronization has completed.

4. Configure multi-language information.

```gradle
android {
    defaultConfig {
        resConfigs "en", "zh-rCN"，""Other languages to be supported.""
    }
}
```

### Client development

1. Assigning App Permissions

The Android OS provides two location permissions: `ACCESS_COARSE_LOCATION` (approximate location
permission) and `ACCESS_FINE_LOCATION` (precise location permission). You need to apply for the
permissions in the Manifest file.

```
<uses-permission android:name="android.permission.ACCESS_COARES_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

In Android Q, if your app needs to continuously locate the device location when it runs in the
background, you need to apply for the `ACCESS_BACKGROUND_LOCATION` permission in the Manifest file.

```
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
```

2. Ask for permissions and handle permisson, device state, and location events

Define text resources when asking the user for permissions:

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

Example for German strings when requesting permissions:
```xml
<resources>
    <string name="error_location_disabled_short">Standortbestimmung deaktiviert</string>
    <string name="error_location_disabled">Die Standortbestimmung ist auf Ihrem Gerät deaktiviert. Sie können dies unter Einstellungen ändern.</string>
    <string name="error_location_disabled_cancel">Abbrechen</string>
    <string name="error_location_disabled_goto_settings">Einstellungen</string>
</resources>
```

Inside your Kotlin file:

```kotlin
    Locator.getCurrentLocation(this, permissionRationale) { locationEvent ->
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

           EventType.LOCATION_DISABLED_ON_DEVICE -> {
                onLocationStillDisabled()
            }
        }
    }
```
