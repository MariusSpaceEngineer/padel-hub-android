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
import com.example.android_project.models.Reservation
import com.example.android_project.utils.ReservationsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReservationsFragment : Fragment() {

    private lateinit var adapter: ReservationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reservations, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ReservationsAdapter()
        recyclerView.adapter = adapter

        fetchReservations()

        return view
    }

    private fun fetchReservations() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        db.collection("reservations")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "Document data: ${document.data}") // Log the document data

                    // Use local variables instead of a data class
                    val clubId = document.getString("clubId")
                    val players = document.get("players") as List<String>?
                    val reservedTimestamp = document.getTimestamp("reservedTimestamp")
                    val userId = document.getString("userId")
                    val matchType = document.getString("matchType")
                    val genderType = document.getString("genderType")

                    // Create a new Reservation object
                    val reservation = Reservation(clubId, players, reservedTimestamp, userId, matchType, genderType)

                    // Add the reservation to the adapter
                    adapter.addReservation(reservation)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }


}
