import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_project.R
import com.example.android_project.databinding.FragmentReservationListBinding
import com.example.android_project.fragments.ConfigureReservationFragment
import com.example.android_project.fragments.ReservationDetailsFragment
import com.example.android_project.models.UserReservation
import com.example.android_project.services.PadelClubService
import com.example.android_project.utils.ReservationListAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class ReservationListFragment : Fragment(), ReservationListAdapter.OnItemClickListener {
    //Inject PadelClub service
    private val _padelClubService = PadelClubService()
    private var _binding: FragmentReservationListBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var adapter: ReservationListAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReservationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        fetchReservations()
    }

    private fun setupViews() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ReservationListAdapter(this)
        binding.recyclerView.adapter = adapter

        progressBar = binding.progressBar
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchReservations() {
        // Get the current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Get the current time in the user's timezone
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val currentTimestamp = Timestamp(calendar.timeInMillis / 1000, 0)

        // Show the ProgressBar
        progressBar.visibility = View.VISIBLE

        // Launch a coroutine to fetch the reservations
        lifecycleScope.launch {
            try {
                // Fetch the reservations
                val reservations = _padelClubService.fetchReservations(userId!!, currentTimestamp)

                // Update the UI with the fetched reservations
                reservations.forEach { reservation ->
                    adapter.addReservation(reservation)
                }

                // If there are no matches, show the tvNoMatches TextView
                if (reservations.isEmpty()) {
                    binding.tvNoReservations.visibility = View.VISIBLE
                }

                // Hide the ProgressBar
                progressBar.visibility = View.GONE
            } catch (e: Exception) {
                // Log the error
                Log.e("YourFragment", "Error fetching reservations", e)

                // Show a user-friendly error message
                Snackbar.make(requireView(), "Error fetching reservations. Please try again.", Snackbar.LENGTH_LONG).show()

                // Hide the ProgressBar
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onItemClick(reservation: UserReservation) {
        val fragment = ConfigureReservationFragment.newInstance(reservation)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit() // Replace 'R.id.container' with your actual container ID
    }

    override fun onItemDetailClick(reservation: UserReservation) {
        val action = ReservationDetailsFragment.newInstance(reservation)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, action)
            .addToBackStack(null)
            .commit()
    }
}
