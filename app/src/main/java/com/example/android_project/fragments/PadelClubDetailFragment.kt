package com.example.android_project.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.R
import com.example.android_project.models.PadelClub
import com.example.android_project.models.Reservation
import com.example.android_project.utils.CustomItemDecoration
import com.example.android_project.utils.TimestampAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PadelClubDetailFragment : Fragment() {

    private lateinit var padelClub: PadelClub
    private lateinit var tvName: TextView
    private lateinit var tvLocation: TextView
    private  lateinit var btnSelectDate : Button
    private lateinit var rvTimestamps: RecyclerView
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvSelectedTime: TextView
    private lateinit var rgMatchType: RadioGroup
    private lateinit var rgGenderType: RadioGroup
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

    }

    private fun setupViews(view: View) {
        // Get references to your views
        tvName = view.findViewById(R.id.tvName)
        tvLocation = view.findViewById(R.id.tvLocation)
        btnSelectDate = view.findViewById<Button>(R.id.btnSelectDate)
        rvTimestamps = view.findViewById<RecyclerView>(R.id.rvTimestamps)
        tvSelectedDate= view.findViewById<TextView>(R.id.tvSelectedDate)
        tvSelectedTime= view.findViewById<TextView>(R.id.tvSelectedTime)
        rgMatchType = view.findViewById<RadioGroup>(R.id.rgMatchType)
        rgGenderType = view.findViewById<RadioGroup>(R.id.rgGenderType)
        btnBookCourt = view.findViewById<Button>(R.id.btnBookCourt)
        btnBookCourt.isEnabled = false

        // Add listeners to your views
        btnSelectDate.setOnClickListener {
            showDatePickerDialog()
        }
        rgMatchType.setOnCheckedChangeListener { _, _ ->
            checkFieldsAndToggleButton()
        }
        rgGenderType.setOnCheckedChangeListener { _, _ ->
            checkFieldsAndToggleButton()
        }

        // Set the data
        tvName.text = padelClub.name
        tvLocation.text = "Location: ${padelClub.location}"

        // Set the LayoutManager to horizontal
        rvTimestamps.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        // Add item decoration to RecyclerView
        rvTimestamps.addItemDecoration(CustomItemDecoration(2))  // replace 10 with your desired space
    }

    private fun checkFieldsAndToggleButton() {
        // Check if a timestamp, match type, and gender type are selected
        if (selectedItem != null && rgMatchType.checkedRadioButtonId != -1 && rgGenderType.checkedRadioButtonId != -1) {
            // If they are, enable the button
            btnBookCourt.isEnabled = true
        } else {
            // If they're not, disable the button
            btnBookCourt.isEnabled = false
        }
    }


    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // This gets called when a date is selected
                // You can use selectedYear, selectedMonth, and selectedDayOfMonth here
                generateTimestamps(selectedYear, selectedMonth, selectedDayOfMonth)
                fetchReservedTimestamps(selectedYear, selectedMonth, selectedDayOfMonth)
                val selectedDate = "$selectedDayOfMonth/${selectedMonth+1}/$selectedYear"
                tvSelectedDate.text = "Selected date: $selectedDate"
                selectedItem = null;
                tvSelectedTime.text = null;
                checkFieldsAndToggleButton()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }



    private fun generateTimestamps(year: Int, month: Int, dayOfMonth: Int) {
        // Generate default timestamps from 12:00 to 18:00 every 30 minutes
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth, 12, 0, 0)

        timestamps = mutableListOf()
        for (i in 0 until 13) { // 13 half hours from 12:00 to 18:00
            val date = calendar.time
            timestamps.add(Timestamp(date)) // Convert to Firestore Timestamp
            calendar.add(Calendar.MINUTE, 30)
        }
    }

    private fun fetchReservedTimestamps(year: Int, month: Int, dayOfMonth: Int) {
        val db = FirebaseFirestore.getInstance()

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
                    reservedTimestamps = mutableListOf()
                    for (document in documents!!) {
                        val reservedTimestamp = document.get("reservedTimestamp") as Timestamp
                        reservedTimestamps.add(reservedTimestamp)
                        Log.d(
                            TAG,
                            "Fetched reserved timestamp: $reservedTimestamp"
                        )  // Log the fetched timestamp
                    }
                    // Now you have the reservedTimestamps, you can setup the RecyclerView
                    setupRecyclerView()
                    setupButton(requireView())
                } else {
                    // The query did not complete successfully
                    Log.w(TAG, "Error getting documents: ", task.exception)
                }
            }
    }


    private fun setupRecyclerView() {
        rvTimestamps.adapter =
            TimestampAdapter(timestamps, reservedTimestamps, { timestamp ->
                selectedItem = timestamp
                val formattedTimestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate())
                tvSelectedTime.text ="Selected time: $formattedTimestamp"
                checkFieldsAndToggleButton()
            }, { timestamp ->
                checkFieldsAndToggleButton()
            })

    }

    private fun setupButton(view: View) {
        // Set click listener for your button
        btnBookCourt.setOnClickListener {
            // Check if a timestamp, match type, and gender type are selected
            if (selectedItem != null && rgMatchType.checkedRadioButtonId != -1 && rgGenderType.checkedRadioButtonId != -1) {
                // Get the currently logged-in user
                val user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    // Get the selected match type and gender type
                    val selectedMatchTypeId = rgMatchType.checkedRadioButtonId
                    val selectedGenderTypeId = rgGenderType.checkedRadioButtonId
                    val matchType = view.findViewById<RadioButton>(selectedMatchTypeId).text.toString()
                    val genderType = view.findViewById<RadioButton>(selectedGenderTypeId).text.toString()

                    // Create a new reservation
                    val reservation = Reservation(
                        clubId = padelClub.id!!,
                        userId = user.uid, // use the user's ID
                        reservedTimestamp = selectedItem!!,
                        players = listOf(user.uid), // use the user's ID
                        matchType = matchType,
                        genderType = genderType
                    )

                    // Convert the Reservation object to a HashMap
                    val reservationMap = hashMapOf(
                        "clubId" to reservation.clubId,
                        "userId" to reservation.userId,
                        "reservedTimestamp" to reservation.reservedTimestamp,
                        "players" to reservation.players,
                        "matchType" to reservation.matchType,
                        "genderType" to reservation.genderType
                    )

                    // Add the new reservation to the Reservations collection in Firestore
                    val db = FirebaseFirestore.getInstance()
                    db.collection("reservations")
                        .add(reservationMap)
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
            } else {
                // Show a Snackbar notification
                Snackbar.make(view, "Please select a timestamp, match type, and gender type.", Snackbar.LENGTH_LONG).show()
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



