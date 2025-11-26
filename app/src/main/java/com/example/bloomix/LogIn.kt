package com.example.bloomix

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LogIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.emailLoginInput)
        val password = findViewById<EditText>(R.id.passwordLoginInput)
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val signupBtn = findViewById<Button>(R.id.goToSignUp)

        // Login button logic
        loginBtn.setOnClickListener {
            val em = email.text.toString().trim()
            val pw = password.text.toString().trim()

            if (em.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(em, pw).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    Toast.makeText(this, "Logged in!", Toast.LENGTH_SHORT).show()

                    // 👉 Redirect user to CalendarActivity after login
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                    finish()  // Prevent back navigation to login

                } else {
                    Toast.makeText(
                        this,
                        task.exception?.message ?: "Login failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Go to signup screen
        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }
}
