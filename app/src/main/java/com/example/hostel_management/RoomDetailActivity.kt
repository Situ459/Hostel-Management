package com.example.hostel_management

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvRoommate1Name: TextView
    private lateinit var tvRoommate1Reg: TextView
    private lateinit var tvRoommate2Name: TextView
    private lateinit var tvRoommate2Reg: TextView
    private lateinit var btnChangeRoom: Button

    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

        toolbar = findViewById(R.id.toolbar)
        tvRoommate1Name = findViewById(R.id.tvRoommate1Name)
        tvRoommate1Reg = findViewById(R.id.tvRoommate1Reg)
        tvRoommate2Name = findViewById(R.id.tvRoommate2Name)
        tvRoommate2Reg = findViewById(R.id.tvRoommate2Reg)
        btnChangeRoom = findViewById(R.id.btnChangeRoom)

        firebaseHelper = FirebaseHelper()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "Room Details"

        loadRoommates()
        setupListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadRoommates() {
        val currentUserId = firebaseHelper.getCurrentUserId() ?: return

        lifecycleScope.launch {
            val userDetails = firebaseHelper.getUserDetails(currentUserId)
            val currentRoom = userDetails?.roomNumber ?: "Not Assigned"
            val allRoommates = withContext(Dispatchers.IO) {
                firebaseHelper.getAllUsersInRoom(currentRoom)
            }.filter { it.regNumber != userDetails?.regNumber }

            if (allRoommates.isNotEmpty()) {
                tvRoommate1Name.text = allRoommates[0].name
                tvRoommate1Reg.text = allRoommates[0].regNumber
            } else {
                tvRoommate1Name.text = "Empty"
                tvRoommate1Reg.text = ""
            }

            if (allRoommates.size > 1) {
                tvRoommate2Name.text = allRoommates[1].name
                tvRoommate2Reg.text = allRoommates[1].regNumber
            } else {
                tvRoommate2Name.text = "Empty"
                tvRoommate2Reg.text = ""
            }
        }
    }


    private fun setupListeners() {
        btnChangeRoom.setOnClickListener {
            Toast.makeText(this, "Change Room coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}
