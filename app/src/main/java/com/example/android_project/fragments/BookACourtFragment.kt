package com.example.android_project.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.R
import com.example.android_project.models.PadelClub
import com.example.android_project.utils.PadelClubAdapter
import com.google.firebase.firestore.FirebaseFirestore

class BookACourtFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_a_court, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get a reference to Firestore
        val db = FirebaseFirestore.getInstance()

        // Get a reference to your RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        // Set the LayoutManager
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch the data
        db.collection("padelClubs")
            .get()
            .addOnSuccessListener { result ->
                val padelClubs = result.map { document ->
                    val padelClub = document.toObject(PadelClub::class.java)
                    padelClub.id = document.id // Set the document ID
                    Log.d("BookACourtFragment", "Fetched PadelClub: $padelClub")
                    padelClub
                }
                recyclerView.adapter = PadelClubAdapter(padelClubs) { padelClub ->
                    val fragment = PadelClubDetailFragment.newInstance(padelClub)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

            .addOnFailureListener { exception ->
                Log.w("BookACourtFragment", "Error getting documents: ", exception)
            }
    }


}




