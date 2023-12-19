package com.example.android_project.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserReservation(
    val documentId: String? = null,
    val clubName: String? = null,
    val players: List<String>? = null,
    val reservedTimestamp: Timestamp? = null,
    val isMatch: Boolean? = null,
    val matchType: String? = null,
    val genderType: String? = null
) : Parcelable


