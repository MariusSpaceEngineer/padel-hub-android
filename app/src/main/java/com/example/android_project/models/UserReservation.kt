package com.example.android_project.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserReservation(
    val clubName: String?, // This will be fetched later using clubId
    val players: List<String>?, // This is a list of players
    val reservedTimestamp: Timestamp?, // This will be shown as a date
    val isMatch: Boolean?, // This will be used for later logic
    val matchType: String?, // This is the match type
    val genderType: String? // This is the gender type
) : Parcelable

