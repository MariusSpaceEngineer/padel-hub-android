import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.Timestamp



class ReservationAdapter(
    private var reservations: List<Pair<String, Map<String, Any>>>,
    private val joinClickListener: (String) -> Unit
) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reservation, parent, false)
        return ReservationViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val (reservationId, reservationData) = reservations[position]

        // Populate your views with reservation data
        val clubId = reservationData["clubId"] as? String
        val genderType = reservationData["genderType"] as? String
        val matchType = reservationData["matchType"] as? String
        val reservedTimestamp = reservationData["reservedTimestamp"] as? Timestamp ?: Timestamp.now()

        val date = reservedTimestamp.toDate()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = sdf.format(date)
        Log.d("ReservationAdapter", "Reserved timestamp: $reservedTimestamp")

        val players = reservationData["players"] as? List<*>
        var clubName: String? = null

        if (clubId != null) {
            db.collection("clubs").document(clubId).get().addOnSuccessListener { document ->
                if (document != null) {
                    clubName = document.data?.get("name") as? String

                    holder.tvClubId.text = "Club: $clubName"
                    holder.tvGenderType.text = "Gender: $genderType"
                    holder.tvMatchType.text = "Match Type: $matchType"
                    holder.tvReservedTimestamp.text = "Reserved Time: $formattedTime"
                    holder.tvPeopleCount.text = "People: ${players?.size ?: 0}/4"

                    Log.d("ReservationAdapter", "DocumentSnapshot data: ${document.data}")
                    Log.d("ReservationAdapter", "Club name: $clubName")
                } else {
                    Log.d("ReservationAdapter", "No such document")
                }
            }.addOnFailureListener { exception ->
                Log.d("ReservationAdapter", "get failed with ", exception)
            }
        }

        // Set onClickListener for the "Join" button
        holder.btnJoin.setOnClickListener { joinClickListener(reservationId) }
    }

    override fun getItemCount(): Int = reservations.size

    fun setReservations(reservations: List<Pair<String, Map<String, Any>?>>) {
        this.reservations = reservations as List<Pair<String, Map<String, Any>>>
        notifyDataSetChanged()
    }

    class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvClubId: TextView = itemView.findViewById(R.id.tvClubId)
        val tvGenderType: TextView = itemView.findViewById(R.id.tvGenderType)
        val tvMatchType: TextView = itemView.findViewById(R.id.tvMatchType)
        val tvReservedTimestamp: TextView = itemView.findViewById(R.id.tvReservedTimestamp)
        val tvPeopleCount: TextView = itemView.findViewById(R.id.tvPeopleCount)
        val btnJoin: Button = itemView.findViewById(R.id.btnJoin)
    }
}