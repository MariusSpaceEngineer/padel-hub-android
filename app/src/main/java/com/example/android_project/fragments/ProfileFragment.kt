package com.example.android_project.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.android_project.LoginActivity
import com.example.android_project.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment(), PreferencesDialogFragment.PreferencesChangeListener {

    private var fullName: String = ""
    private var bestHand: String = ""
    private var courtPosition: String = ""
    private var matchType: String = ""

    private lateinit var logoutButton: Button
    private lateinit var playButton: Button
    private lateinit var editPreferences: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
    }



    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(com.example.android_project.R.layout.fragment_profile, container, false)

        logoutButton = view.findViewById(R.id.logoutButton)
        playButton = view.findViewById(R.id.playButton)
        editPreferences = view.findViewById(R.id.editPreferences)

        editPreferences.setOnClickListener {
            //implement preferences pop up here
            val preferencesDialog = PreferencesDialogFragment()
            preferencesDialog.setPreferencesChangeListener(this)
            preferencesDialog.show(requireFragmentManager(), "PreferencesDialog")
        }

        logoutButton.setOnClickListener {
            // Call signOut to log the user out
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        playButton.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)

            // Select the 'Home' item in the BottomNavigationView
            bottomNav.selectedItemId = R.id.joinGame
        }

        return view

    }

    private fun loadData() {
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val db = FirebaseFirestore.getInstance()
        val userDocument = user?.uid?.let { db.collection("userInformation").document(it) }
        if (userDocument != null){
            userDocument.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        fullName = document.getString("name").toString()
                        val preferences = document.get("preferences") as Map<*, *>
                        bestHand = preferences["bestHand"].toString()
                        courtPosition = preferences["courtPosition"].toString()
                        matchType = preferences["matchType"].toString()
                        Log.d("ProfileFragment", "fullname is now $fullName")
                        val usernameText = view?.findViewById<TextView>(com.example.android_project.R.id.username)
                        val bestHandText = view?.findViewById<TextView>(com.example.android_project.R.id.bestHand)
                        val courtPositionText = view?.findViewById<TextView>(com.example.android_project.R.id.courtPosition)
                        val matchTypeText = view?.findViewById<TextView>(com.example.android_project.R.id.matchType)
                        if (usernameText != null) {
                            usernameText.text = fullName
                        }
                        if(bestHandText != null && courtPositionText != null && matchTypeText != null){
                            bestHandText.text = bestHand
                            courtPositionText.text = courtPosition
                            matchTypeText.text = matchType
                        }
                        else{
                            Log.d("ProfileFragment", "v is null")
                        }
                    } else {
                        Log.d("ProfileFragment", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("ProfileFragment", "get failed with ", exception)
                }

        }

        Log.d("ProfileFragment", "User: $user")
        Log.d("ProfileFragment", "Email: ${user?.email}")
        Log.d("ProfileFragment", "Name: ${user?.uid}")
    }

    override fun onPreferencesChanged() {
        loadData()
    }


}