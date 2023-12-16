package com.example.android_project.fragments

import ReservationsFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.android_project.R
import com.example.android_project.models.UserReservation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ConfigureReservationFragment : Fragment() {

    private lateinit var reservation: UserReservation
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reservation = arguments?.getParcelable("reservation") ?: UserReservation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_configure_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clubName: TextView = view.findViewById(R.id.clubName)
        val reservedTimestamp: TextView = view.findViewById(R.id.reservedTimestamp)
        val rgMatchType: RadioGroup = view.findViewById(R.id.rgMatchType)
        val rgGenderType: RadioGroup = view.findViewById(R.id.rgGenderType)
        val saveButton: Button = view.findViewById(R.id.saveButton)
        saveButton.isEnabled = false // Disable the button initially


        clubName.text = "Club Name: ${reservation.clubName}"
        val timestamp = reservation.reservedTimestamp?.toDate()
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateString = formatter.format(timestamp)
        // You'll need to format the reservedTimestamp into a readable format
        reservedTimestamp.text = "Reserved Timestamp: $dateString"

        // Enable the button when both radio groups have a selection
        rgMatchType.setOnCheckedChangeListener { _, _ ->
            saveButton.isEnabled = rgMatchType.checkedRadioButtonId != -1 && rgGenderType.checkedRadioButtonId != -1
        }
        rgGenderType.setOnCheckedChangeListener { _, _ ->
            saveButton.isEnabled = rgMatchType.checkedRadioButtonId != -1 && rgGenderType.checkedRadioButtonId != -1
        }

        when (reservation.matchType) {
            "Competitive" -> rgMatchType.check(R.id.rbCompetitive)
            "Friendly" -> rgMatchType.check(R.id.rbFriendly)
        }

        when (reservation.genderType) {
            "All Players" -> rgGenderType.check(R.id.rbAllPlayers)
            "Mixed" -> rgGenderType.check(R.id.rbMixed)
            "Men Only" -> rgGenderType.check(R.id.rbMenOnly)
            "Women Only" -> rgGenderType.check(R.id.rbWomenOnly)
        }

        saveButton.setOnClickListener {
            val matchType = when (rgMatchType.checkedRadioButtonId) {
                R.id.rbCompetitive -> "Competitive"
                R.id.rbFriendly -> "Friendly"
                else -> null
            }

            val genderType = when (rgGenderType.checkedRadioButtonId) {
                R.id.rbAllPlayers -> "All Players"
                R.id.rbMixed -> "Mixed"
                R.id.rbMenOnly -> "Men Only"
                R.id.rbWomenOnly -> "Women Only"
                else -> null
            }

            if (matchType != null && genderType != null) {
                val reservationMap = hashMapOf(
                    "matchType" to matchType,
                    "genderType" to genderType,
                    "isMatch" to true
                )

                val nonNullMap = reservationMap.filterValues { it != null }

                db.collection("reservations")
                    .document(reservation.documentId.toString()) // Replace with the ID of the document to update
                    .update(nonNullMap)
                    .addOnSuccessListener {
                        Snackbar.make(
                            view,
                            "Reservation updated successfully",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        // Navigate to a specific Fragment
                        val fragment =
                            ReservationsFragment() // Replace with your target Fragment class
                        parentFragmentManager.beginTransaction().replace(R.id.container, fragment)
                            .commit() // Replace 'R.id.container' with your actual container ID
                    }
                    .addOnFailureListener { e ->
                        Snackbar.make(
                            view,
                            "Error updating reservation: ${e.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

            } else {
                Snackbar.make(
                    view,
                    "Please select both match type and gender type",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

        }
    }
}
