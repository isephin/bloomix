package com.example.bloomix

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUp : AppCompatActivity() {

    // Firebase Auth instance for creating users
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // 1. INITIALIZE THE ML PROCESSOR FIRST
        // This gives it permission to read 'model.json' and 'training_data.json'
        MLProcessor.initialize(this)

        // --- DEVELOPMENT TOOL ---
        // This runs the Naive Bayes accuracy test and prints results to Logcat.
        // use "ModelEval" on logcat searchbar to get results
        ModelEvaluator.runEvaluation(this)

        // 1. Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // 2. Bind UI Elements
        val nickname = findViewById<EditText>(R.id.nicknameInput)
        val email = findViewById<EditText>(R.id.emailInput)
        val password = findViewById<EditText>(R.id.passwordInput)
        val createButton = findViewById<Button>(R.id.createAccountButton)
        val loginButton = findViewById<TextView>(R.id.goToLogin)

        // 3. Create Account Button Logic
        createButton.setOnClickListener {
            val nn = nickname.text.toString().trim()
            val em = email.text.toString().trim()
            val pw = password.text.toString().trim()

            // Validation: Ensure no fields are empty
            if (nn.isEmpty() || em.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validation: Firebase requires passwords to be at least 6 chars
            if (pw.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. Create User in Firebase
            auth.createUserWithEmailAndPassword(em, pw).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Success: Save nickname locally (since Firebase Auth doesn't store nicknames easily)
                    val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
                    prefs.edit().putString("user_nickname", nn).apply()

                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()

                    // Navigate to Login Screen (or directly to Calendar if you prefer)
                    startActivity(Intent(this, LogIn::class.java))
                    finish()
                } else {
                    // Failure: Show error (e.g., "Email already in use")
                    Toast.makeText(this, task.exception?.message ?: "Signup failed", Toast.LENGTH_LONG).show()
                }
            }
        }

        // 5. Navigation: Already have an account? Go to Login.
        loginButton.setOnClickListener {
            startActivity(Intent(this, LogIn::class.java))
        }
    }
}