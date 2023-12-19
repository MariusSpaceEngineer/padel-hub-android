import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.R
import com.example.android_project.fragments.ConfigureReservationFragment
import com.example.android_project.models.UserReservation
import com.example.android_project.utils.ReservationListAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ReservationListFragment : Fragment(), ReservationListAdapter.OnItemClickListener {
    private lateinit var adapter: ReservationListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reservations, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ReservationListAdapter(this)
        recyclerView.adapter = adapter

        fetchReservations()

        return view
    }

    private fun fetchReservations() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        // Get the current time in the user's timezone
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val currentTimestamp = Timestamp(calendar.timeInMillis / 1000, 0)

        db.collection("reservations")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("reservedTimestamp", currentTimestamp)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Show a message when there are no reservations
                    view?.let { Snackbar.make(it, "There are no reservations.", Snackbar.LENGTH_SHORT).show() }
                } else {
                    for (document in documents) {
                        // Fetch the clubName using the clubId
                        val clubId = document.getString("clubId")
                        CoroutineScope(Dispatchers.IO).launch {
                            val clubName = fetchClubName(clubId)

                            // Use local variables instead of a data class
                            val documentId = document.id
                            val players = document.get("players") as List<String>?
                            val reservedTimestamp = document.getTimestamp("reservedTimestamp")
                            val isMatch = document.getBoolean("isMatch")
                            val matchType = document.getString("matchType")
                            val genderType = document.getString("genderType")

                            // Create a new ReservationData object
                            val reservationData = UserReservation(documentId,clubName, players, reservedTimestamp, isMatch, matchType, genderType)

                            // Add the reservation to the adapter
                            withContext(Dispatchers.Main) {
                                adapter.addReservation(reservationData)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

    }


    private suspend fun fetchClubName(clubId: String?): String? = suspendCoroutine { continuation ->
        // Initialize Firestore
        val db = FirebaseFirestore.getInstance()

        // Fetch the club document using the clubId
        db.collection("clubs").document(clubId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    continuation.resume(document.getString("name"))
                } else {
                    Log.d(TAG, "No such document")
                    continuation.resume(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
                continuation.resumeWithException(exception)
            }
    }

    override fun onItemClick(reservation: UserReservation) {
        val fragment = ConfigureReservationFragment()
        val bundle = Bundle()
        bundle.putParcelable("reservation", reservation)
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.container, fragment).commit() // Replace 'R.id.container' with your actual container ID
    }

}
