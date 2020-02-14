# Getting location on Android

This lib is an easy way to get the location inside Android applications. The new thing about it, it either uses Google Play Services (if installed) or Huawei Mobile Services (https://developer.huawei.com/consumer/en/service/hms/locationservice.html).

## Location use cases

Handling locations is complex. This lib handles all known use cases on Android devices, when trying to get the location.

### S1 - success szenario
Gegeben: Keine Standortberechtigung erteilt
Sobald der Nutzer den auf Screen “Standorteinstellungen” geht, wird der Berechtigungsdialog angezeigt
Berechtigung wird akzeptiert
Es wird nach Standort gesucht, währenddessen erscheint ein Ladebalken
Der aktuelle Standort konnte gefunden werden
Aktueller Standort wird übernommen

### S2 - location not found
Gegeben: Keine Standortberechtigung erteilt und Nutzer ist auf Screen “Standorteinstellungen”
Sobald der Nutzer den auf Screen “Standorteinstellungen” geht, wird der Berechtigungsdialog angezeigt
Berechtigung wird akzeptiert
Es wird nach Standort gesucht, währenddessen erscheint ein Ladebalken
Der aktuelle Standort konnte nicht gefunden werden
Standort bleibt unverändert + Anzeige Fehlermeldung als Snackbar-Meldung “Ihr Standort konnte nicht gefunden werden.”

### S3 - location permission declined
Gegeben: Keine Standortberechtigung erteilt und Nutzer ist auf Screen “Standorteinstellungen”
Sobald der Nutzer den auf Screen “Standorteinstellungen” geht, wird der Berechtigungsdialog angezeigt
Berechtigung wird nicht akzeptiert
Standort bleibt unverändert + keine Fehlermeldung anzeigen

### S4 - location authorisation is later revoked
Gegeben: Standortberechtigung bereits erteilt, aber anschließend wieder entzogen
Sobald der Nutzer den auf Screen “Standorteinstellungen” geht, wird der Berechtigungsdialog angezeigt
Berechtigungsdialog soll erneut angezeigt werden, danach S1/S2/S3

### S5 - location has been deactivated throughout the OS
Gegeben: Die Standortbestimmung auf dem Gerät ist deaktiviert
Standarddialog: “Die Standortbestimmung ist auf Ihrem Gerät deaktiviert. Sie können dies unter Einstellungen ändern.” mit Buttons “Abbrechen” und “Einstellungen”
Verlinkung zu Einstellungsseite “Standort” im OS

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
=======
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
