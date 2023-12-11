package com.example.android_project.fragments

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.R
import com.example.android_project.models.PadelClub
import com.example.android_project.utils.TimestampAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class PadelClubDetailFragment : Fragment() {

    private lateinit var padelClub: PadelClub
    private lateinit var tvName: TextView
    private lateinit var tvLocation: TextView
    private lateinit var rvTimestamps: RecyclerView
    private lateinit var btnBookCourt: Button
    private lateinit var timestamps: MutableList<Timestamp>
    private lateinit var reservedTimestamps: MutableList<Timestamp>
    private var selectedItem: Timestamp? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        padelClub = arguments?.getParcelable("padelClub") ?: PadelClub()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_padel_club_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        generateTimestamps()
        fetchReservedTimestamps()
    }

    private fun setupViews(view: View) {
        // Get references to your views
        tvName = view.findViewById(R.id.tvName)
        tvLocation = view.findViewById(R.id.tvLocation)
        rvTimestamps = view.findViewById<RecyclerView>(R.id.rvTimestamps)
        btnBookCourt = view.findViewById<Button>(R.id.btnBookCourt)

        // Set the data
        tvName.text = padelClub.name
        tvLocation.text = "Location: ${padelClub.location}"

        // Set the LayoutManager to horizontal
        rvTimestamps.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun generateTimestamps() {
        // Generate default timestamps from 12:00 to 18:00 every 30 minutes
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        timestamps = mutableListOf()
        for (i in 0 until 13) { // 13 half hours from 12:00 to 18:00
            val date = calendar.time
            timestamps.add(Timestamp(date)) // Convert to Firestore Timestamp
            calendar.add(Calendar.MINUTE, 30)
        }
    }

    private fun fetchReservedTimestamps() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Reservations")
            .whereEqualTo("clubId", padelClub.id)
            .get()
            .addOnSuccessListener { documents ->
                reservedTimestamps = mutableListOf()
                for (document in documents) {
                    val reservedTimestamp = document.get("reservedTimestamp") as Timestamp
                    reservedTimestamps.add(reservedTimestamp)
                }

                // Now you have the reservedTimestamps, you can setup the RecyclerView
                setupRecyclerView()
                setupButton(requireView())
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun setupRecyclerView() {
        // Disable the button initially
        btnBookCourt.isEnabled = false

        rvTimestamps.adapter =
            TimestampAdapter(timestamps, reservedTimestamps) { timestamp ->
                selectedItem = timestamp
                // Enable the button when a timestamp is selected
                btnBookCourt.isEnabled = true
            }
    }

    private fun setupButton(view: View) {
        // Set click listener for your button
        btnBookCourt.setOnClickListener {
            // Check if a timestamp is selected
            selectedItem?.let { selectedTimestamp ->
                // Get the currently logged-in user
                val user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    // Create a new reservation
                    val reservation = hashMapOf(
                        "clubId" to padelClub.id,
                        "userId" to user.uid, // use the user's ID
                        "reservedTimestamp" to selectedTimestamp,
                        "players" to listOf(user.uid) // use the user's ID
                    )

                    // Add the new reservation to the Reservations collection in Firestore
                    val db = FirebaseFirestore.getInstance()
                    db.collection("Reservations")
                        .add(reservation)
                        .addOnSuccessListener { documentReference ->
                            Log.d(
                                "PadelClubDetailFragment",
                                "Reservation added with ID: ${documentReference.id}"
                            )
                            // Show a Snackbar notification
                            Snackbar.make(view, "Reservation successful!", Snackbar.LENGTH_LONG).show()
                            // Pop the current fragment off the back stack to navigate back to the previous fragment
                            parentFragmentManager.popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Log.w("PadelClubDetailFragment", "Error adding reservation", e)
                            // Show an AlertDialog notification
                            AlertDialog.Builder(context)
                                .setTitle("Reservation")
                                .setMessage("Reservation unsuccessful!")
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
                        }
                }
            }
        }
    }

    companion object {
        fun newInstance(padelClub: PadelClub) = PadelClubDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable("padelClub", padelClub)
            }
        }
    }

}



