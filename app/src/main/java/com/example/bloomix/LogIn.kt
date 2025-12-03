package com.example.bloomix

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LogIn : AppCompatActivity() {

    // Firebase Auth instance used to handle sign-in operations
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 2. Bind UI elements
        val email = findViewById<EditText>(R.id.emailLoginInput)
        val password = findViewById<EditText>(R.id.passwordLoginInput)
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val signupBtn = findViewById<Button>(R.id.goToSignUp)

        // 3. Login Button Click Listener
        loginBtn.setOnClickListener {
            val em = email.text.toString().trim()
            val pw = password.text.toString().trim()

            // Input Validation: Check if fields are empty
            if (em.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. Attempt to sign in with Firebase
            auth.signInWithEmailAndPassword(em, pw).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Success: Notify user
                    Toast.makeText(this, "Logged in!", Toast.LENGTH_SHORT).show()

                    // Navigate to the main Dashboard (CalendarActivity)
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)

                    // Finish this activity so the user cannot go back to the Login screen by pressing "Back"
                    finish()

                } else {
                    // Failure: Show the error message returned by Firebase (e.g., "Wrong password")
                    Toast.makeText(
                        this,
                        task.exception?.message ?: "Login failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // 5. Navigation to Sign Up screen if user doesn't have an account
        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }
}