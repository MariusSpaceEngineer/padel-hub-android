package com.example.android_project

import android.content.Intent
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
    private lateinit var buttonRegister: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("LoginPage", "Activity created")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        if(FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        else {
            editTextEmail = findViewById(R.id.email)
            editTextPassword = findViewById(R.id.password)
            buttonLogin = findViewById(R.id.loginButton)
            buttonRegister = findViewById(R.id.registerButton)

            buttonLogin.setOnClickListener {
                Log.d("LoginPage", "Login button clicked")
                val email = editTextEmail.text.toString().trim()
                val password = editTextPassword.text.toString().trim()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    loginUser(email, password)
                }
            }

            buttonRegister.setOnClickListener {
                Log.d("LoginPage", "Register button clicked")
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        Log.d("LoginPage", "Login activated")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("LoginPage", "Login successful")
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.d("LoginPage", "Login unsuccessful")
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("No access")
                        builder.setMessage("Login unsuccessful")
                        builder.show()
                    }
                }
        }
    }