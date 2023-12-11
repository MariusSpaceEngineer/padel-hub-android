package com.example.android_project.fragments

import android.app.AlertDialog
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class PadelClubDetailFragment : Fragment() {

    private lateinit var padelClub: PadelClub

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

        // Get references to your views
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
        val tvCourts = view.findViewById<TextView>(R.id.tvCourts)
        val rvTimestamps = view.findViewById<RecyclerView>(R.id.rvTimestamps)
        val btnBookCourt = view.findViewById<Button>(R.id.btnBookCourt)

        // Disable the button initially
        btnBookCourt.isEnabled = false

        // Set the data
        tvName.text = padelClub.name
        tvLocation.text = "Location: Latitude - ${padelClub.location.latitude}, Longitude - ${padelClub.location.longitude}"
        tvCourts.text = "Courts: ${padelClub.courts.size}"

        // Set the LayoutManager to horizontal
        rvTimestamps.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Generate default timestamps from 12:00 to 18:00 every 30 minutes
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val timestamps = mutableListOf<Timestamp>()
        for (i in 0 until 13) { // 13 half hours from 12:00 to 18:00
            val date = calendar.time
            timestamps.add(Timestamp(date)) // Convert to Firestore Timestamp
            calendar.add(Calendar.MINUTE, 30)
        }




        var selectedItem: Timestamp? = null
        rvTimestamps.adapter = TimestampAdapter(timestamps, padelClub.reservedTimestamps) { timestamp ->
            selectedItem = timestamp
            // Enable the button when a timestamp is selected
            btnBookCourt.isEnabled = true
        }

        // Set click listener for your button
        btnBookCourt.setOnClickListener {
            // Check if a timestamp is selected
            selectedItem?.let { selectedTimestamp ->
                // Update the reservedTimestamps of the PadelClub in Firestore
                val db = FirebaseFirestore.getInstance()
                db.collection("padelClubs").document(padelClub.id!!)
                    .update("reservedTimestamps", FieldValue.arrayUnion(selectedTimestamp))
                    .addOnSuccessListener {
                        Log.d("PadelClubDetailFragment", "PadelClub updated with ID: ${padelClub.id}")
                        // Show a Snackbar notification
                        Snackbar.make(view, "Reservation successful!", Snackbar.LENGTH_LONG).show()
                        // Pop the current fragment off the back stack to navigate back to the previous fragment
                        parentFragmentManager.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Log.w("PadelClubDetailFragment", "Error updating PadelClub", e)
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

    companion object {
        fun newInstance(padelClub: PadelClub) = PadelClubDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable("padelClub", padelClub)
            }
        }
    }
}


