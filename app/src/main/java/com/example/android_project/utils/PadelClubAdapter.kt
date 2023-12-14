package com.example.android_project.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android_project.R
import com.example.android_project.models.PadelClub

class PadelClubAdapter(private val padelClubs: List<PadelClub>, private val onClick: (PadelClub) -> Unit) : RecyclerView.Adapter<PadelClubAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val ivPicture: ImageView = view.findViewById(R.id.ivPicture)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.padel_club_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val padelClub = padelClubs[position]
        holder.tvName.text = "Name: ${padelClub.name}"
        holder.tvLocation.text = "Location: ${padelClub.location}"
        Glide.with(holder.itemView.context)
            .load(padelClub.picture)
            .into(holder.ivPicture)

        holder.itemView.setOnClickListener { onClick(padelClub) }

    }


    override fun getItemCount() = padelClubs.size
}


