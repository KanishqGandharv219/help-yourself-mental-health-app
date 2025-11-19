package com.helpyourself.com.utils

import android.location.Location
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Suspends until a single high-accuracy location fix is returned or `timeoutMs` expires.
 * Returns null on timeout or cancellation.
 */
suspend fun FusedLocationProviderClient.awaitHighAccuracyFix(
    timeoutMs: Long = 10_000L
): Location? = suspendCancellableCoroutine { cont ->
    val request = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        0L /* interval */
    ).setMaxUpdates(1).build()

    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            cont.resume(result.lastLocation)
            removeLocationUpdates(this)
        }

        override fun onLocationAvailability(p0: LocationAvailability) {}
    }

    requestLocationUpdates(request, callback, Looper.getMainLooper())

    cont.invokeOnCancellation { removeLocationUpdates(callback) }

    // Manual timeout
    Handler(Looper.getMainLooper()).postDelayed({
        if (cont.isActive) {
            removeLocationUpdates(callback)
            cont.resume(null)
        }
    }, timeoutMs)
} 