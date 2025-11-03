package com.example.hostel_management

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubmitComplaintActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvUserName: TextView
    private lateinit var tvRegNumber: TextView
    private lateinit var tvRoomNumber: TextView
    private lateinit var cbElectricity: CheckBox
    private lateinit var cbWater: CheckBox
    private lateinit var cbFood: CheckBox
    private lateinit var cbCleanliness: CheckBox
    private lateinit var cbOthers: CheckBox
    private lateinit var etComplaintDetails: EditText
    private lateinit var btnSubmit: Button

    private lateinit var firebaseHelper: FirebaseHelper

    // Hold current user details to use while submitting complaint
    private var currentUserDetails: UserDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_complaint)

        toolbar = findViewById(R.id.toolbar)
        tvUserName = findViewById(R.id.tvUserName)
        tvRegNumber = findViewById(R.id.tvRegNumber)
        tvRoomNumber = findViewById(R.id.tvRoomNumber)
        cbElectricity = findViewById(R.id.cbElectricity)
        cbWater = findViewById(R.id.cbWater)
        cbFood = findViewById(R.id.cbFood)
        cbCleanliness = findViewById(R.id.cbCleanliness)
        cbOthers = findViewById(R.id.cbOthers)
        etComplaintDetails = findViewById(R.id.etComplaintDetails)
        btnSubmit = findViewById(R.id.btnSubmit)

        firebaseHelper = FirebaseHelper()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Submit Complaint"

        toolbar.setNavigationOnClickListener {
            finish()
        }

        loadUserData()
        setupSubmitListener()
    }

    private fun loadUserData() {
        val currentUserId = firebaseHelper.getCurrentUserId() ?: return
        lifecycleScope.launch {
            val userDetails = withContext(Dispatchers.IO) {
                firebaseHelper.getUserDetails(currentUserId)
            }
            if (userDetails != null) {
                currentUserDetails = userDetails
                tvUserName.text = "Name: ${userDetails.name}"
                tvRegNumber.text = "Registration No.: ${userDetails.regNumber}"
                tvRoomNumber.text = "Room No.: ${userDetails.roomNumber}"
            }
        }
    }

    private fun setupSubmitListener() {
        btnSubmit.setOnClickListener {
            val complaintTypes = mutableListOf<String>()
            if (cbElectricity.isChecked) complaintTypes.add("Electricity")
            if (cbWater.isChecked) complaintTypes.add("Water")
            if (cbFood.isChecked) complaintTypes.add("Food")
            if (cbCleanliness.isChecked) complaintTypes.add("Cleanliness")
            if (cbOthers.isChecked) complaintTypes.add("Others")

            val complaintText = etComplaintDetails.text.toString().trim()

            if (complaintTypes.isEmpty()) {
                Toast.makeText(this, "Please select at least one complaint category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (complaintText.isEmpty()) {
                Toast.makeText(this, "Please enter complaint details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userDetails = currentUserDetails
            if (userDetails == null) {
                Toast.makeText(this, "User data not loaded yet. Please try again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    firebaseHelper.submitComplaint(
                        regNumber = userDetails.regNumber,
                        name = userDetails.name,
                        roomNumber = userDetails.roomNumber,
                        complaintCategories = complaintTypes,
                        complaintText = complaintText
                    )
                }

                if (result.isSuccess) {
                    Toast.makeText(this@SubmitComplaintActivity,
                        "Complaint submitted successfully!",
                        Toast.LENGTH_LONG).show()

                    // Clear inputs after submission
                    cbElectricity.isChecked = false
                    cbWater.isChecked = false
                    cbFood.isChecked = false
                    cbCleanliness.isChecked = false
                    cbOthers.isChecked = false
                    etComplaintDetails.text.clear()
                } else {
                    Toast.makeText(this@SubmitComplaintActivity,
                        "Failed to submit complaint: ${result.exceptionOrNull()?.localizedMessage}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
