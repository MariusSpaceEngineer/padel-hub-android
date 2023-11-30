package com.example.android_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.example.android_project.R

class PadelClubsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_padel_clubs)

        // Get a reference to Firestore
        val db = FirebaseFirestore.getInstance()

        // Get a reference to your TextView
        val textView = findViewById<TextView>(R.id.textView)

        // Fetch the data
        db.collection("padelClubs")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val data = document.data
                    val details = data.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                    // Update your TextView
                    Log.d("result", details)
                    textView.append(details + "\n\n")
                }
            }
            .addOnFailureListener { exception ->
                textView.text = "Error getting documents: $exception"
            }

    }
}
