import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class JoinGameFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_join_game, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Add a divider between items
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            (recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        adapter = ReservationAdapter(emptyList()) { reservationId -> joinReservation(reservationId) }
        recyclerView.adapter = adapter

        loadReservations()

        return view
    }

    private fun loadReservations() {
        val now = Date()
        val sevenDaysFromNow = Date(now.time + 7 * 24 * 60 * 60 * 1000) // 7 days in milliseconds

        db.collection("reservations")
            .whereGreaterThan("reservedTimestamp", now)
            .whereLessThan("reservedTimestamp", sevenDaysFromNow)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Filter the results based on the array length
                val filteredReservations = querySnapshot.documents.filter { doc ->
                    val players = doc["players"] as? List<*>
                    players?.size ?: 0 < 4
                }

                val reservations = filteredReservations.map { it.id to it.data } // Pairing reservation ID with data
                Log.d("JoinGameFragment", "Reservations: $reservations")
                adapter.setReservations(reservations)
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.e("JoinGameFragment", "Error getting reservations", exception)
            }
    }

    private fun joinReservation(reservationId: String) {
        val user = auth.currentUser?.uid ?: return
        val reservationRef = db.collection("reservations").document(reservationId)

        db.runTransaction { transaction ->
            val reservation = transaction.get(reservationRef)
            val players = reservation["players"] as? List<*>

            if ((players?.size ?: 0) < 4 && !players?.contains(user)!!) {
                transaction.update(reservationRef, "players", players.plus(user))
            } else {
                    showToast("You are already in this match!")
            }

            null
        }.addOnSuccessListener {
            showToast("Successfully joined the match!")
            loadReservations()
            Log.d("JoinGameFragment", "Transaction success!")
        }.addOnFailureListener { e ->
            Log.w("JoinGameFragment", "Transaction failure.", e)
        }
    }

    private fun showToast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
