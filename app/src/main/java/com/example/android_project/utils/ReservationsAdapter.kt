package com.example.android_project.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.R
import com.example.android_project.models.UserReservation
import java.text.SimpleDateFormat
import java.util.Locale

class ReservationsAdapter : RecyclerView.Adapter<ReservationsAdapter.ViewHolder>() {

    private val reservations = mutableListOf<UserReservation>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val clubName: TextView = view.findViewById(R.id.clubName)
        val genderType: TextView = view.findViewById(R.id.genderType)
        val matchType: TextView = view.findViewById(R.id.matchType)
        val players: TextView = view.findViewById(R.id.players)
        val reservedTimestamp: TextView = view.findViewById(R.id.reservedTimestamp)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reservation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.clubName.text = "Club Name: ${reservation.clubName}"
        holder.matchType.text = "Match Type: ${reservation.matchType}"
        holder.genderType.text = "Gender Type : ${reservation.genderType}"
        holder.players.text = "Number Of Players: ${reservation.players?.size}"

        val timestamp = reservation.reservedTimestamp?.toDate()
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateString = formatter.format(timestamp)
        holder.reservedTimestamp.text = "Reserved Timestamp: $dateString"
    }

    override fun getItemCount() = reservations.size

    fun addReservation(reservation: UserReservation) {
        reservations.add(reservation)
        reservations.sortBy { it.reservedTimestamp }
        notifyDataSetChanged()
    }

}
