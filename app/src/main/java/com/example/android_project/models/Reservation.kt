package com.example.android_project.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reservation(
    val clubId: String?,
    val players: List<String>?,
    val reservedTimestamp: Timestamp?,
    val isMatch: Boolean?,
    val userId: String?,
    val matchType: String?,
    val genderType: String?
) : Parcelable

