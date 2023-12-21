package com.example.android_project.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.android_project.databinding.FragmentPadelClubOverviewBinding
import com.example.android_project.models.PadelClub
import com.example.android_project.models.Reservation
import com.example.android_project.services.PadelClubService
import com.example.android_project.utils.CustomItemDecoration
import com.example.android_project.utils.TimestampAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PadelClubOverviewFragment : Fragment() {
    //Inject PadelClub service
    private val _padelClubService = PadelClubService()

    private var _binding: FragmentPadelClubOverviewBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var padelClub: PadelClub
    private lateinit var timestamps: MutableList<Timestamp>
    private lateinit var reservedTimestamps: List<Timestamp>
    private var selectedTimestamp: Timestamp? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        padelClub = arguments?.getParcelable("padelClub") ?: PadelClub()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPadelClubOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
    }

    private fun setupViews(view: View) {
        //Initialize reservedTimestamps list
        reservedTimestamps = listOf()
        // Disable button at startup
        binding.btnBookCourt.isEnabled= false;
        // Uncheck the checkbox at startup
        binding.cbMatchCheck.isChecked = false

        // Hide related fields to a public match
        binding.tvSelectMatch.visibility = View.GONE
        binding.tvSelectGender.visibility = View.GONE
        binding.rgMatchType.visibility = View.GONE
        binding.rgGenderType.visibility = View.GONE

        // Clear the radio group selections
        binding.rgMatchType.clearCheck()
        binding.rgGenderType.clearCheck()

        // Add listeners
        binding.btnSelectDate.setOnClickListener {
            showDatePickerDialog()
        }
        //Show MatchType and GenderType after the checkbox
        // for a public match has been checked
        binding.cbMatchCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvSelectMatch.visibility = View.VISIBLE
                binding.tvSelectGender.visibility = View.VISIBLE
                binding.rgMatchType.visibility = View.VISIBLE
                binding.rgGenderType.visibility = View.VISIBLE

                // Fetch the gender and disable the RadioButton
                // Get the current user ID
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                lifecycleScope.launch {
                    _padelClubService.fetchGenderAndDisableRadioButton(userId!!, binding.rbWomenOnly, binding.rbMenOnly)
                }
            } else {
                binding.tvSelectMatch.visibility = View.GONE
                binding.tvSelectGender.visibility = View.GONE
                binding.rgMatchType.visibility = View.GONE
                binding.rgGenderType.visibility = View.GONE

                // Set the checked radio button id to -1 (which indicates no selection)
                binding.rgMatchType.clearCheck()
                binding.rgGenderType.clearCheck()
            }
            //Check all fields at startup
            checkFieldsAndToggleButton()
        }

        binding.rgMatchType.setOnCheckedChangeListener { _, _ ->
            checkFieldsAndToggleButton()
        }
        binding.rgGenderType.setOnCheckedChangeListener { _, _ ->
            checkFieldsAndToggleButton()
        }

        // Set the padel club data to the text views
        binding.tvName.text = padelClub.name
        binding.tvLocation.text = "Location: ${padelClub.location}"

        // Set the LayoutManager for the timestamps to horizontal
        binding.rvTimestamps.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        // Add item decoration to RecyclerView
        binding.rvTimestamps.addItemDecoration(CustomItemDecoration(2))  // replace 10 with your desired space

        //Load padel club pictures from firestore
        Glide.with(this)
            .load(padelClub?.picture)
            .into(binding.ivPicture)
    }

    private fun checkFieldsAndToggleButton() {
        // Check if a timestamp is selected and either the checkmark is unchecked or both radio groups have a selection
        binding.btnBookCourt.isEnabled = selectedTimestamp != null
                && (!binding.cbMatchCheck.isChecked ||
                (binding.rgMatchType.checkedRadioButtonId != -1
                && binding.rgGenderType.checkedRadioButtonId != -1))
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
                generateTimestamps(selectedYear, selectedMonth, selectedDayOfMonth)
                fetchReservedTimestamps(padelClub.id!!, selectedYear, selectedMonth, selectedDayOfMonth)
                //Format date for the textView
                val selectedDate = "$selectedDayOfMonth/${selectedMonth+1}/$selectedYear"
                binding.tvSelectedDate.text = "Selected date: $selectedDate"
                selectedTimestamp = null;
                binding.tvSelectedTime.text = null;
                checkFieldsAndToggleButton()
            },
            year,
            month,
            day
        )
        //Limits the calendar to be able to select the current day
        // or future dates only
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
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

    private fun fetchReservedTimestamps(clubId: String, year: Int, month: Int, dayOfMonth: Int) {
        _padelClubService.fetchReservedTimestamps(clubId, year, month, dayOfMonth, { timestamps ->
            reservedTimestamps = timestamps
            setupRecyclerView()
            setupButton()
        }, { exception ->
            Log.w("PadelClubOverviewFragment", "Error getting documents: ", exception)
        })
    }

    private fun setupRecyclerView() {
        binding.rvTimestamps.adapter =
            TimestampAdapter(timestamps, reservedTimestamps, { timestamp ->
                selectedTimestamp = timestamp
                val formattedTimestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate())
                binding.tvSelectedTime.text ="Selected time: $formattedTimestamp"
                checkFieldsAndToggleButton()
            }, {
                checkFieldsAndToggleButton()
            })

    }

    private fun setupButton() {
        binding.btnBookCourt.setOnClickListener {
            if (isSelectionValid()) {
                val user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    val reservationMap = createReservationMap(it)
                    addReservationToFirestore(reservationMap)
                }
            } else {
                showSnackbar("Please select a timestamp, match type, and gender type.")
            }
        }
    }
    private fun isSelectionValid(): Boolean {
        return selectedTimestamp != null && (!binding.cbMatchCheck.isChecked || (binding.rgMatchType.checkedRadioButtonId != -1 && binding.rgGenderType.checkedRadioButtonId != -1))
    }

    private fun createReservationMap(user: FirebaseUser): HashMap<String, Any?> {
        val matchType = getSelectedRadioButtonText(binding.rgMatchType)
        val genderType = getSelectedRadioButtonText(binding.rgGenderType)

        val reservation = Reservation(
            clubId = padelClub.id!!,
            userId = user.uid,
            reservedTimestamp = selectedTimestamp!!,
            players = listOf(user.uid),
            isMatch = binding.cbMatchCheck.isChecked,
            matchType = matchType,
            genderType = genderType
        )

        return hashMapOf(
            "clubId" to reservation.clubId,
            "userId" to reservation.userId,
            "reservedTimestamp" to reservation.reservedTimestamp,
            "players" to reservation.players,
            "isMatch" to reservation.isMatch,
            "matchType" to reservation.matchType,
            "genderType" to reservation.genderType
        )
    }

    private fun getSelectedRadioButtonText(radioGroup: RadioGroup): String? {
        val selectedId = radioGroup.checkedRadioButtonId
        val radioButton = view?.findViewById<RadioButton>(selectedId)
        return radioButton?.text?.toString()
    }

    private fun addReservationToFirestore(reservationMap: HashMap<String, Any?>) {
        _padelClubService.addReservationToFirestore(reservationMap, { documentId ->
            Log.d("PadelClubDetailFragment", "Reservation added with ID: $documentId")
            showSnackbar("Reservation successful!")
            parentFragmentManager.popBackStack()
        }, { exception ->
            Log.w("PadelClubDetailFragment", "Error adding reservation", exception)
            showAlertDialog("Reservation", "Reservation unsuccessful!")
        })
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        fun newInstance(padelClub: PadelClub) = PadelClubOverviewFragment().apply {
            arguments = Bundle().apply {
                putParcelable("padelClub", padelClub)
            }
        }
    }
}



