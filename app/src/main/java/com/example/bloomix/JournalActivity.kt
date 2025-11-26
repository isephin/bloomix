package com.example.bloomix

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class JournalActivity : AppCompatActivity() {

    private var selectedDateKey: String? = null
    private var chosenFlowerKey: String = "white_daisy"
    private var selectedEmotions: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        selectedDateKey = intent.getStringExtra("selectedDate")
        chosenFlowerKey = intent.getStringExtra("flower_key") ?: "white_daisy"
        selectedEmotions = intent.getStringArrayListExtra("selected_emotions") ?: arrayListOf()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnBloomOpen).setOnClickListener {
            showBloomPopup()
        }
    }

    private fun showBloomPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_let_it_bloom)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<ImageView>(R.id.btnClosePopup).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnLetItBloom).setOnClickListener {
            openFlowerResultScreen()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openFlowerResultScreen() {
        val journalText = findViewById<EditText>(R.id.etJournal).text.toString()

        // Save to SharedPreferences
        selectedDateKey?.let { dateKey ->
            val prefs = getSharedPreferences("journal_data", MODE_PRIVATE)
            prefs.edit().apply {
                putString("flower_$dateKey", chosenFlowerKey)
                putString("journal_$dateKey", journalText)
            }.apply()
        }

        // START FlowerResultActivity
        val intent = Intent(this, FlowerResultActivity::class.java)
        intent.putExtra("selected", selectedEmotions)
        intent.putExtra("journal", journalText)
        intent.putExtra("flower_key", chosenFlowerKey)

        startActivity(intent)
        finish()
    }
}
