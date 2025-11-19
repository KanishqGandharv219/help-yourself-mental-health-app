package com.helpyourself.com.models

import com.google.android.gms.maps.model.LatLng

data class PlaceData(
    val id: String,
    val name: String,
    val address: String,
    val latLng: LatLng,
    val rating: Double
)
