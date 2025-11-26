package com.example.bloomix

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<Button?>(R.id.goToLogin)
        val btnSignUp = findViewById<Button?>(R.id.goToSignUp)
        btnLogin?.setOnClickListener {
            startActivity(Intent(this, LogIn::class.java))
        }
        btnSignUp?.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }
}
