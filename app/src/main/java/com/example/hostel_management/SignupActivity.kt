package com.example.hostel_management

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var etRegNumber: TextInputEditText
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSelectRoom: Button
    private lateinit var tvSelectedRoom: TextView
    private lateinit var btnSignup: Button
    private lateinit var tvLogin: TextView

    private lateinit var rgUserRole: RadioGroup
    private lateinit var rbStudent: RadioButton
    private lateinit var rbAdmin: RadioButton
    private lateinit var tilAdminSecret: TextInputLayout
    private lateinit var etAdminSecret: TextInputEditText

    private lateinit var firebaseHelper: FirebaseHelper
    private var selectedRoom: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        firebaseHelper = FirebaseHelper()

        etRegNumber = findViewById(R.id.etRegNumber)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSelectRoom = findViewById(R.id.btnSelectRoom)
        tvSelectedRoom = findViewById(R.id.tvSelectedRoom)
        btnSignup = findViewById(R.id.btnSignup)
        tvLogin = findViewById(R.id.tvLogin)

        rgUserRole = findViewById(R.id.rgUserRole)
        rbStudent = findViewById(R.id.rbStudent)
        rbAdmin = findViewById(R.id.rbAdmin)
        tilAdminSecret = findViewById(R.id.tilAdminSecret)
        etAdminSecret = findViewById(R.id.etAdminSecret)

        rgUserRole.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbAdmin) {
                tilAdminSecret.visibility = View.VISIBLE
                btnSelectRoom.visibility = View.GONE
                tvSelectedRoom.visibility = View.GONE
                selectedRoom = "" // Clear room selection for admin
            } else {
                tilAdminSecret.visibility = View.GONE
                btnSelectRoom.visibility = View.VISIBLE
                tvSelectedRoom.visibility = View.VISIBLE
            }
        }

        btnSelectRoom.setOnClickListener {
            showRoomSelectionDialog()
        }

        btnSignup.setOnClickListener {
            val regNumber = etRegNumber.text.toString().trim()
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val role = if (rbAdmin.isChecked) "admin" else "student"
            val adminSecret = etAdminSecret.text?.toString()?.trim() ?: ""

            if (!validateInput(regNumber, name, email, password, confirmPassword, role, adminSecret)) {
                return@setOnClickListener
            }
            signupUser(regNumber, name, email, password, role)
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInput(
        regNumber: String,
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String,
        adminSecret: String
    ): Boolean {
        if (regNumber.isEmpty()) {
            etRegNumber.error = "Registration number required"
            return false
        }
        if (name.isEmpty()) {
            etName.error = "Name required"
            return false
        }
        if (email.isEmpty()) {
            etEmail.error = "Email required"
            return false
        }
        if (password.isEmpty()) {
            etPassword.error = "Password required"
            return false
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return false
        }
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Confirm password required"
            return false
        }
        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            return false
        }
        if (role == "student" && selectedRoom.isEmpty()) {
            Toast.makeText(this, "Please select a room", Toast.LENGTH_SHORT).show()
            return false
        }
        if (role == "admin" && adminSecret != "ADMIN2025!") {
            tilAdminSecret.error = "Invalid admin secret code"
            return false
        } else {
            tilAdminSecret.error = null
        }
        return true
    }

    private fun showRoomSelectionDialog() {
        btnSelectRoom.isEnabled = false
        btnSelectRoom.text = "Loading rooms..."

        lifecycleScope.launch {
            val availableRooms = firebaseHelper.getAvailableRooms()

            btnSelectRoom.isEnabled = true
            btnSelectRoom.text = "Select Room"

            if (availableRooms.isEmpty()) {
                Toast.makeText(this@SignupActivity, "No rooms available. Hostel is full.", Toast.LENGTH_LONG).show()
                return@launch
            }

            val roomNames = availableRooms.map { "${it.roomNumber} (${it.occupancy}/3 occupied)" }.toTypedArray()

            AlertDialog.Builder(this@SignupActivity)
                .setTitle("Select Your Room")
                .setItems(roomNames) { _, which ->
                    selectedRoom = availableRooms[which].roomNumber
                    tvSelectedRoom.text = "Selected: $selectedRoom"
                    tvSelectedRoom.setTextColor(getColor(R.color.green_dark))
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun signupUser(regNumber: String, name: String, email: String, password: String, role: String) {
        btnSignup.isEnabled = false
        btnSignup.text = "Creating Account..."

        lifecycleScope.launch {
            val result = firebaseHelper.signUpUser(email, password, regNumber, name, selectedRoom, role)

            if (result.isSuccess) {
                val userId = result.getOrNull() ?: ""
                val userDetails = firebaseHelper.getUserDetails(userId)
                Toast.makeText(this@SignupActivity, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                if (userDetails != null && userDetails.role == "admin") {
                    startActivity(Intent(this@SignupActivity, AdminDashboardActivity::class.java))
                } else {
                    startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                }
                finish()
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Signup failed"
                Toast.makeText(this@SignupActivity, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                btnSignup.isEnabled = true
                btnSignup.text = "Sign Up"
            }
        }
    }
}
