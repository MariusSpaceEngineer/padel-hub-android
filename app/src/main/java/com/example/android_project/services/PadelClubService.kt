package com.example.android_project.services

import android.util.Log
import com.example.android_project.models.PadelClub
import com.example.android_project.models.UserReservation
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar

class PadelClubService {
    private val db = FirebaseFirestore.getInstance()

    fun fetchPadelClubs(onSuccess: (List<PadelClub>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("clubs")
            .get()
            .addOnSuccessListener { result ->
                val padelClubs = result.map { document ->
                    val padelClub = document.toObject(PadelClub::class.java)
                    padelClub.id = document.id // Set the document ID
                    Log.d("FirestoreService", "Fetched PadelClub: $padelClub")
                    padelClub
                }
                onSuccess(padelClubs)
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreService", "Error getting documents: ", exception)
                onFailure(exception)
            }
    }

    fun fetchReservedTimestamps(year: Int, month: Int, dayOfMonth: Int, onSuccess: (List<Timestamp>) -> Unit, onFailure: (Exception) -> Unit) {
        // Create a range for the selected date
        val startCalendar = Calendar.getInstance()
        startCalendar.set(year, month, dayOfMonth, 0, 0, 0)  // Start of the day
        val startDate = Timestamp(startCalendar.time)

        val endCalendar = Calendar.getInstance()
        endCalendar.set(year, month, dayOfMonth, 23, 59, 59)  // End of the day
        val endDate = Timestamp(endCalendar.time)

        // Fetch only the reservations for the selected date
        db.collection("reservations")
            .whereGreaterThanOrEqualTo("reservedTimestamp", startDate)
            .whereLessThanOrEqualTo("reservedTimestamp", endDate)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // The query completed successfully
                    val documents = task.result
                    val reservedTimestamps = mutableListOf<Timestamp>()
                    for (document in documents!!) {
                        val reservedTimestamp = document.get("reservedTimestamp") as Timestamp
                        reservedTimestamps.add(reservedTimestamp)
                        Log.d("FirestoreService", "Fetched reserved timestamp: $reservedTimestamp")  // Log the fetched timestamp
                    }
                    onSuccess(reservedTimestamps)
                } else {
                    // The query did not complete successfully
                    Log.w("FirestoreService", "Error getting documents: ", task.exception)
                    onFailure(task.exception!!)
                }
            }
    }

    fun addReservationToFirestore(reservationMap: HashMap<String, Any?>, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("reservations")
            .add(reservationMap)
            .addOnSuccessListener { documentReference ->
                Log.d("FirestoreService", "Reservation added with ID: ${documentReference.id}")
                onSuccess(documentReference.id)
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreService", "Error adding reservation", e)
                onFailure(e)
            }
    }

    suspend fun fetchReservations(userId: String, currentTimestamp: Timestamp): List<UserReservation> {
        return withContext(Dispatchers.IO) {
            val task = db.collection("reservations")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("reservedTimestamp", currentTimestamp)
                .get()
                .await()

            task.documents.mapNotNull { document ->
                val clubId = document.getString("clubId")
                val clubName = fetchClubName(clubId)
                if (clubName != null) {
                    val documentId = document.id
                    val players = document.get("players") as List<String>?
                    val reservedTimestamp = document.getTimestamp("reservedTimestamp")
                    val isMatch = document.getBoolean("isMatch")
                    val matchType = document.getString("matchType")
                    val genderType = document.getString("genderType")
                    UserReservation(documentId, clubName, players, reservedTimestamp, isMatch, matchType, genderType)
                } else {
                    null
                }
            }
        }
    }

    private suspend fun fetchClubName(clubId: String?): String? {
        return withContext(Dispatchers.IO) {
            val document = db.collection("clubs").document(clubId!!).get().await()
            document.getString("name")
        }
    }

    fun updateReservation(documentId: String, matchType: String, genderType: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val reservationMap = hashMapOf(
            "matchType" to matchType,
            "genderType" to genderType,
            "isMatch" to true
        )

        val nonNullMap = reservationMap.filterValues { it != null }

        db.collection("reservations")
            .document(documentId)
            .update(nonNullMap)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}


