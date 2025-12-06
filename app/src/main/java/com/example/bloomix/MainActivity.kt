package com.example.bloomix

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // 1. Initialize "Go to Login" button
        val btnLogin = findViewById<Button?>(R.id.goToLogin)

        // 2. Set Click Listener to open the Login Activity
        btnLogin?.setOnClickListener {
            startActivity(Intent(this, LogIn::class.java))
        }

        // 3. Initialize "Go to Sign Up" button
        val btnSignUp = findViewById<Button?>(R.id.goToSignUp)

        // 4. Set Click Listener to open the Sign Up Activity
        btnSignUp?.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }
}