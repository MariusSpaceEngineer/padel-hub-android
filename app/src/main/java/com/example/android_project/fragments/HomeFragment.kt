package com.example.android_project.fragments

import JoinGameFragment
import ReservationListFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.android_project.R

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val ivBookCourt: ImageView = view.findViewById(R.id.ivBookCourt)
        Glide.with(this)
            .load("https://firebasestorage.googleapis.com/v0/b/mobile-jordi-marius.appspot.com/o/book_padel.jpg?alt=media&token=ead2f34e-9449-46b3-9295-2f4f8cbd663a")
            .into(ivBookCourt)

        val ivSeeReservations: ImageView = view.findViewById(R.id.ivSeeReservations)
        Glide.with(this)
            .load("https://firebasestorage.googleapis.com/v0/b/mobile-jordi-marius.appspot.com/o/see_reservations.jpg?alt=media&token=ff2cdc65-6b1f-45f3-86fe-76f354994807")
            .into(ivSeeReservations)

        val ivJoinMatch: ImageView = view.findViewById(R.id.ivJoinMatch)
        Glide.with(this)
            .load("https://firebasestorage.googleapis.com/v0/b/mobile-jordi-marius.appspot.com/o/join_existing_match.jpg?alt=media&token=1166f734-f90a-4c0b-a8d0-7d498d704dfb")
            .into(ivJoinMatch)

        val cardBookCourt: CardView = view.findViewById(R.id.cardBookCourt)
        cardBookCourt.setOnClickListener {
            val fragment = PadelClubListFragment() // Replace with your fragment class
            parentFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
        }

        val cardSeeReservations: CardView = view.findViewById(R.id.cardSeeReservations)
        cardSeeReservations.setOnClickListener {
            val fragment = ReservationListFragment() // Replace with your fragment class
            parentFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
        }

        val cardJoinMatch: CardView = view.findViewById(R.id.cardJoinMatch)
        cardJoinMatch.setOnClickListener {
            val fragment = JoinGameFragment() // Replace with your fragment class
            parentFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
        }

        return view
    }
}
