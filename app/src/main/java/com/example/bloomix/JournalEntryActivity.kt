package com.example.bloomix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

class JournalEntryActivity : AppCompatActivity() {

    private lateinit var contentInput: EditText
    private lateinit var saveButton: FloatingActionButton
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Link the activity to the XML layout file
        setContentView(R.layout.activity_journal)

        // 1. Initialize Views
        contentInput = findViewById(R.id.et_journal_content)
        saveButton = findViewById(R.id.fab_save)
        backButton = findViewById(R.id.btn_back)

        // 2. Handle Save Button Click (FAB)
        saveButton.setOnClickListener {
            val content = contentInput.text.toString().trim()

            if (content.isNotEmpty()) {
                // In a real app, you would save 'content' to a database (Room/SQLite) here.
                // For demonstration, we just show a message.
                Toast.makeText(this, "Journal entry saved!", Toast.LENGTH_SHORT).show()
                // Optionally, close the activity
                finish()
            } else {
                Toast.makeText(this, "Entry cannot be empty.", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Handle Back Button Click
        backButton.setOnClickListener {
            // Closes the current activity and returns to the previous screen
            finish()
        }
    }
}