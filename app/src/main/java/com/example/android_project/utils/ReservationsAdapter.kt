package com.example.android_project.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.R
import com.example.android_project.models.Reservation

class ReservationsAdapter : RecyclerView.Adapter<ReservationsAdapter.ViewHolder>() {

    private val reservations = mutableListOf<Reservation>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val clubId: TextView = view.findViewById(R.id.clubId)
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
        holder.clubId.text = reservation.clubId
        holder.genderType.text = reservation.genderType
        holder.matchType.text = reservation.matchType
        holder.players.text = reservation.players!!.joinToString(", ")
        holder.reservedTimestamp.text = reservation.reservedTimestamp.toString()
    }

    override fun getItemCount() = reservations.size

    fun addReservation(reservation: Reservation) {
        reservations.add(reservation)
        notifyItemInserted(reservations.size - 1)
    }
}
