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
    val location: String = ""
) : Parcelable


