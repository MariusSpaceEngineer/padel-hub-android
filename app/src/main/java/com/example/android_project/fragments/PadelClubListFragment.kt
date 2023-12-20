package com.example.android_project.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.R
import com.example.android_project.services.PadelClubService
import com.example.android_project.utils.PadelClubAdapter

class PadelClubListFragment : Fragment() {
    //Inject PadelClub service
    private val _padelClubService = PadelClubService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_padel_club_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get a reference to the RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        // Set the LayoutManager
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch the data
        _padelClubService.fetchPadelClubs({ padelClubs ->
            recyclerView.adapter = PadelClubAdapter(padelClubs) { padelClub ->
                val fragment = PadelClubOverviewFragment.newInstance(padelClub)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }, { exception ->
            Log.w("BookACourtFragment", "Error getting documents: ", exception)
        })
    }
}




