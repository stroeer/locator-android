package de.stroeer.locator_android

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LocationPermissionRationaleMessage(var rationaleTitle: String = "Location permission required",
                                              var rationaleMessage: String = "Location permission is required in order to show you weather forecast. Please grant permissions.",
                                              var rationaleYes: String = "Yes",
                                              var rationaleNo: String = "No") : Parcelable