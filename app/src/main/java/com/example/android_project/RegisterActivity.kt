package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var dropDown: Spinner
    private lateinit var registerButton: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        getComponents()

        createDropdown()

        registerButton.setOnClickListener {
            Log.d("RegisterPage", "Login button clicked")
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val firstName = editTextFirstName.text.toString().trim()
            val lastName = editTextLastName.text.toString().trim()
            val gender = dropDown.selectedItem.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && gender.isNotEmpty()) {
                registerUser(email, password, firstName, lastName, gender)
            }
        }
    }

    private fun registerUser(email: String, password: String, firstName: String, lastName: String, gender: String) {
        Log.d("RegisterPage", "Register activated")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    val uid: String = user?.uid.toString()
                    Log.d("RegistrationActivity", "Registration successful. User UID: ${user?.uid}")

                    val db = FirebaseFirestore.getInstance()
                    val userDocument = db.collection("userInformation").document(uid)

                    val userData = hashMapOf(
                        "name" to "$firstName $lastName",
                        "email" to email,
                        "gender" to gender,
                        "preferences" to hashMapOf(
                            "bestHand" to "UNDEFINED",
                            "courtPosition" to "UNDEFINED",
                            "matchType" to "UNDEFINED"
                        ),
                    )

                    userDocument.set(userData)
                        .addOnSuccessListener {
                            Log.d("RegistrationActivity", "User data added to Firestore.")
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Log.w("RegistrationActivity", "Error adding user data to Firestore", e)
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("Registration failed")
                            builder.setMessage("${task.exception}")
                            builder.show()
                        }
                } else {
                    Log.w("RegistrationActivity", "Registration failed. ${task.exception}")
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Registration failed")
                    builder.setMessage("${task.exception}")
                    builder.show()
                }
            }
    }

    private fun getComponents() {
        editTextEmail = findViewById(R.id.email)
        editTextFirstName = findViewById(R.id.firstName)
        editTextLastName = findViewById(R.id.lastName)
        editTextPassword = findViewById(R.id.password)
        dropDown = findViewById(R.id.genderSelector)
        registerButton = findViewById(R.id.registerButton)
    }

    private fun createDropdown() {
        val genders = resources.getStringArray(R.array.Genders)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        dropDown.adapter = adapter
    }
}