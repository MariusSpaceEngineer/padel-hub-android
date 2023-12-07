package com.example.android_project.utils

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class TimestampAdapter(private val timestamps: List<Timestamp>, private val reservedTimestamps: List<Timestamp>, private val onClick: (Timestamp) -> Unit) : RecyclerView.Adapter<TimestampAdapter.ViewHolder>() {

    var selectedItem: Timestamp? = null

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timestamp = timestamps[position]
        val formattedTimestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate())
        holder.textView.text = formattedTimestamp

        // Check if the timestamp is reserved or within 90 minutes after a reserved timestamp
        if (isReservedOrWithin90Min(timestamp)) {
            holder.textView.setTextColor(Color.RED) // Change text color to red
            holder.itemView.isClickable = false // Disable click event
        } else {
            holder.textView.setTextColor(Color.BLACK) // Change text color to black
            holder.itemView.setOnClickListener {
                selectedItem = timestamp
                onClick(timestamp)
                notifyDataSetChanged() // Notify the adapter to update the views
            }
        }

        // Set the selected state
        holder.textView.isSelected = timestamp == selectedItem
    }

    override fun getItemCount() = timestamps.size

    private fun isReservedOrWithin90Min(timestamp: Timestamp): Boolean {
        // Check if the timestamp is reserved
        if (reservedTimestamps.contains(timestamp)) {
            return true
        }

        // Check if the timestamp is within 90 minutes after a reserved timestamp
        val ninetyMinInMilliseconds = 90 * 60 * 1000
        for (reservedTimestamp in reservedTimestamps) {
            if (reservedTimestamp.toDate().time - timestamp.toDate().time <= ninetyMinInMilliseconds) {
                return true
            }
        }

        return false
    }

}


