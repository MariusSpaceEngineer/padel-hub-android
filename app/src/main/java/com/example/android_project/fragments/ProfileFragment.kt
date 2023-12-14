package com.example.android_project.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.android_project.LoginActivity
import com.example.android_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment() {

    private var fullName: String = ""

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
                        Log.d("ProfileFragment", "fullname is now $fullName")
                        val v = view?.findViewById<TextView>(com.example.android_project.R.id.username)
                        if (v != null) {
                            v.text = fullName
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
}