import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.R

class ReservationAdapter(
    private var reservations: List<Pair<String, Map<String, Any>>>,
    private val joinClickListener: (String) -> Unit
) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reservation, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val (reservationId, reservationData) = reservations[position]

        // Populate your views with reservation data
        val clubId = reservationData["clubId"] as? String
        val genderType = reservationData["genderType"] as? String
        val matchType = reservationData["matchType"] as? String
        val reservedTimestamp = reservationData["reservedTimestamp"] as? String

        holder.tvClubId.text = "Club ID: $clubId"
        holder.tvGenderType.text = "Gender: $genderType"
        holder.tvMatchType.text = "Match Type: $matchType"
        holder.tvReservedTimestamp.text = "Reserved Time: $reservedTimestamp"

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
        val btnJoin: Button = itemView.findViewById(R.id.btnJoin)
    }
}