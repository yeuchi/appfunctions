/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.appfunctions.agent.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** Built-in AppFunctions for location and geocoding services. */
class BuiltInAppFunctions {
    /**
     * Geocodes a physical address string into its latitude and longitude coordinates.
     *
     * @param address The physical address to geocode (e.g., "1600 Amphitheatre Pkwy, Mountain View,
     *   CA").
     * @return The latitude and longitude coordinates of the address, or null if geocoding fails.
     */
    @AppFunction
    suspend fun geocodeAddress(
        appFunctionContext: AppFunctionContext,
        address: String,
    ): LatLng? {
        val context = appFunctionContext.context
        if (!Geocoder.isPresent()) {
            return null
        }

        val geocoder = Geocoder(context)

        return withContext(Dispatchers.IO) {
            try {
                suspendCoroutine { continuation ->
                    geocoder.getFromLocationName(
                        address,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                val location = addresses.firstOrNull()
                                if (location != null) {
                                    continuation.resume(
                                        LatLng(location.latitude, location.longitude),
                                    )
                                } else {
                                    continuation.resume(null)
                                }
                            }

                            override fun onError(errorMessage: String?) {
                                continuation.resume(null)
                            }
                        },
                    )
                }
            } catch (e: Exception) {
                throw IllegalStateException(e.message, e)
            }
        }
    }

    /**
     * Retrieves the current latitude and longitude coordinates of the device.
     *
     * @return The current location coordinates of the device, or null if location is unavailable or
     *   permission is denied.
     */
    @SuppressLint("MissingPermission")
    @AppFunction
    suspend fun getCurrentLocation(appFunctionContext: AppFunctionContext): LatLng? =
        withContext(Dispatchers.Default) {
            val context = appFunctionContext.context

            // Check permissions
            val hasFineLocation =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

            val hasCoarseLocation =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) ==
                    PackageManager.PERMISSION_GRANTED

            if (!hasFineLocation && !hasCoarseLocation) {
                throw IllegalStateException("Location permission is not granted")
            }

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            try {
                // Try GPS Provider first
                var location =
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    } else {
                        null
                    }

                // Fallback to Network Provider if GPS is not available
                if (location == null &&
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ) {
                    location =
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                if (location != null) {
                    LatLng(location.latitude, location.longitude)
                } else {
                    null
                }
            } catch (e: Exception) {
                throw IllegalStateException(e.message, e)
            }
        }

    /** Represents the latitude and longitude coordinates. */
    @AppFunctionSerializable
    data class LatLng(
        /** The latitude coordinate. */
        val latitude: Double,
        /** The longitude coordinate. */
        val longitude: Double,
    )
}
