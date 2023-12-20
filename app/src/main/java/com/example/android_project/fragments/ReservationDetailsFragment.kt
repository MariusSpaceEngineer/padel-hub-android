package com.example.android_project.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.android_project.R
import com.example.android_project.models.UserReservation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class ReservationDetailsFragment : Fragment() {

    private lateinit var clubNameDetail: TextView
    private lateinit var matchTypeDetail: TextView
    private lateinit var genderTypeDetail: TextView
    private lateinit var playersDetail: TextView
    private lateinit var reservedTimestampDetail: TextView
    private lateinit var playerNameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reservation_details, container, false)
        clubNameDetail = view.findViewById(R.id.clubNameDetail)
        matchTypeDetail = view.findViewById(R.id.matchTypeDetail)
        genderTypeDetail = view.findViewById(R.id.genderTypeDetail)
        playersDetail = view.findViewById(R.id.playersDetail)
        reservedTimestampDetail = view.findViewById(R.id.reservedTimestampDetail)
        playerNameTextView = view.findViewById(R.id.playerNameTextView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reservation = arguments?.getParcelable<UserReservation>("reservation")
        reservation?.let {
            displayReservationDetails(it)
            fetchPlayerNames(it.players)
        }
    }

    companion object {
        private const val ARG_RESERVATION = "reservation"

        fun newInstance(reservation: UserReservation): ReservationDetailsFragment {
            val fragment = ReservationDetailsFragment()
            val args = Bundle().apply {
                putParcelable(ARG_RESERVATION, reservation)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private fun displayReservationDetails(reservation: UserReservation) {
        clubNameDetail.text = "Club Name: ${reservation.clubName}"
        matchTypeDetail.text = "Match Type: ${reservation.matchType ?: "None"}"
        genderTypeDetail.text = "Gender Type: ${reservation.genderType ?: "None"}"
        playersDetail.text = "Number Of Players: ${reservation.players?.size ?: 0}"
        playerNameTextView.text = "Player Names:"

        val timestamp = reservation.reservedTimestamp?.toDate()
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateString = formatter.format(timestamp)
        reservedTimestampDetail.text = "Reserved Timestamp: $dateString"
    }

    private fun fetchPlayerNames(playerIds: List<String>?) {
        playerIds?.let {
            val firestore = FirebaseFirestore.getInstance()
            val playerNames = mutableListOf<String>()

            // Use coroutine scope to perform asynchronous Firestore queries
            lifecycleScope.launch {
                for (playerId in playerIds) {
                    try {
                        val playerDocument = firestore.collection("userInformation").document(playerId).get().await()
                        val playerName = playerDocument.getString("name")
                        playerName?.let { playerNames.add(it) }
                    } catch (e: Exception) {
                        // Handle errors, if any
                        Log.e("ReservationDetailsFragment", "Error fetching player name", e)
                    }
                }

                // Update UI with player names
                updatePlayerNamesInView(playerNames)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePlayerNamesInView(playerNames: List<String>) {
        // Update your UI components with player names as needed
        playerNameTextView.text = "Player Names: ${playerNames.joinToString(", ")}"
    }

}
