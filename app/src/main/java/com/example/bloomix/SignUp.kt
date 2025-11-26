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

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        val nickname = findViewById<EditText>(R.id.nicknameInput)
        val email = findViewById<EditText>(R.id.emailInput)
        val password = findViewById<EditText>(R.id.passwordInput)
        val createButton = findViewById<Button>(R.id.createAccountButton)
        val loginButton = findViewById<TextView>(R.id.goToLogin)

        createButton.setOnClickListener {
            val nn = nickname.text.toString().trim()
            val em = email.text.toString().trim()
            val pw = password.text.toString().trim()
            if (nn.isEmpty() || em.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pw.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(em, pw).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LogIn::class.java))
                    finish()
                } else {
                    Toast.makeText(this, task.exception?.message ?: "Signup failed", Toast.LENGTH_LONG).show()
                }
            }
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, LogIn::class.java))
        }
    }
}
