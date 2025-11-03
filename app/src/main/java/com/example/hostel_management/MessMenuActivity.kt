package com.example.hostel_management

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MessMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mess_menu)
        // Optional: set title in app bar
        supportActionBar?.title = "Mess Menu"
    }
}
