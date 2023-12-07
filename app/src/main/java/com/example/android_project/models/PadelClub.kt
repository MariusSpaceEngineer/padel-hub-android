package com.example.android_project.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Parcelize
data class PadelClub(
    var id: String? = null,
    val picture: String? = null,
    val name: String = "",
    val location: @RawValue GeoPoint = GeoPoint(0.0, 0.0),
    val courts: @RawValue List<DocumentReference> = listOf(),
    val reservedTimestamps: List<Timestamp> = listOf()
) : Parcelable

