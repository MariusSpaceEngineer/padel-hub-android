package com.example.android_project.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.android_project.R
import com.example.android_project.databinding.FragmentConfigureReservationBinding
import com.example.android_project.models.UserReservation
import com.example.android_project.services.PadelClubService
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class ConfigureReservationFragment : Fragment() {

    private val _padelClubService = PadelClubService()
    private var _binding: FragmentConfigureReservationBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var reservation: UserReservation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reservation = arguments?.getParcelable("reservation") ?: UserReservation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfigureReservationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViews(view: View) {
        val clubName: TextView = binding.clubName
        val reservedTimestamp: TextView = binding.reservedTimestamp

        clubName.text = "Club Name: ${reservation.clubName}"
        reservedTimestamp.text = "Reserved Timestamp: ${formatDate(reservation.reservedTimestamp)}"
        binding.rgMatchType.check(-1)
        binding.rgGenderType.check(-1)


        when (reservation.matchType) {
            "Competitive" -> binding.rgMatchType.check(R.id.rbCompetitive)
            "Friendly" -> binding.rgMatchType.check(R.id.rbFriendly)
        }

        when (reservation.genderType) {
            "All Players" -> binding.rgGenderType.check(R.id.rbAllPlayers)
            "Mixed" -> binding.rgGenderType.check(R.id.rbMixed)
            "Men Only" -> binding.rgGenderType.check(R.id.rbMenOnly)
            "Women Only" -> binding.rgGenderType.check(R.id.rbWomenOnly)
        }
        setupRadioGroupListeners()
        setupSaveButtonListener(view)
    }

    private fun formatDate(timestamp: Timestamp?): String {
        val date = timestamp?.toDate()
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    private fun setupRadioGroupListeners() {
        binding.rgMatchType.setOnCheckedChangeListener { _, _ ->
            checkRadioGroups()
        }
        binding.rgGenderType.setOnCheckedChangeListener { _, _ ->
            checkRadioGroups()
        }
    }

    private fun checkRadioGroups() {
        binding.saveButton.isEnabled =
            binding.rgMatchType.checkedRadioButtonId != -1 && binding.rgGenderType.checkedRadioButtonId != -1
    }

    private fun setupSaveButtonListener(view: View) {
        binding.saveButton.setOnClickListener {
            val matchType = when (binding.rgMatchType.checkedRadioButtonId) {
                R.id.rbCompetitive -> "Competitive"
                R.id.rbFriendly -> "Friendly"
                else -> null
            }

            val genderType = when (binding.rgGenderType.checkedRadioButtonId) {
                R.id.rbAllPlayers -> "All Players"
                R.id.rbMixed -> "Mixed"
                R.id.rbMenOnly -> "Men Only"
                R.id.rbWomenOnly -> "Women Only"
                else -> null
            }

            if (matchType != null && genderType != null) {
                updateReservation(view, matchType, genderType)
            } else {
                Snackbar.make(
                    view,
                    "Please select both match type and gender type",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateReservation(view: View, matchType: String, genderType: String) {
        _padelClubService.updateReservation(
            reservation.documentId.toString(),
            matchType,
            genderType,
            {
                Snackbar.make(
                    view,
                    "Reservation updated successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
                parentFragmentManager.popBackStack()
            },
            { e ->
                Snackbar.make(
                    view,
                    "Error updating reservation: ${e.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
            })

    }

    companion object {
        fun newInstance(reservation: UserReservation) = ConfigureReservationFragment().apply {
            arguments = Bundle().apply {
                putParcelable("reservation", reservation)
            }
        }
    }
}
