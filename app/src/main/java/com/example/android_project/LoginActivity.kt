package com.example.android_project

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("YourActivity", "Activity created")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        editTextEmail = findViewById(R.id.username)
        editTextPassword = findViewById(R.id.password)
        buttonLogin = findViewById(R.id.loginButton)

        buttonLogin.setOnClickListener {
            print("Login button clicked")
            Log.d("YourActivity", "Login button clicked")
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        Log.d("YourActivity", "Login activated")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Access granted")
                    builder.setMessage("Login successful")
                    builder.show()
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("No access")
                    builder.setMessage("Login unsuccessful")
                    builder.show()
                }
            }
    }
}