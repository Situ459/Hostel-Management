package com.example.hostel_management

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var cbAdminLogin: CheckBox
    private lateinit var tilAdminSecretLogin: TextInputLayout
    private lateinit var etAdminSecretLogin: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignup: TextView
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseHelper = FirebaseHelper()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        cbAdminLogin = findViewById(R.id.cbAdminLogin)
        tilAdminSecretLogin = findViewById(R.id.tilAdminSecretLogin)
        etAdminSecretLogin = findViewById(R.id.etAdminSecretLogin)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignup = findViewById(R.id.tvSignup)

        // Show/hide admin secret code input depending on admin checkbox
        cbAdminLogin.setOnCheckedChangeListener { _, isChecked ->
            tilAdminSecretLogin.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
            if (!isChecked) {
                tilAdminSecretLogin.error = null
                etAdminSecretLogin.text?.clear()
            }
        }

        if (firebaseHelper.isUserLoggedIn()) {
            navigateToDashboard()
            return
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val isAdmin = cbAdminLogin.isChecked
            val adminSecret = etAdminSecretLogin.text?.toString()?.trim() ?: ""

            if (!validateInput(email, password, isAdmin, adminSecret)) return@setOnClickListener

            loginUser(email, password, isAdmin, adminSecret)
        }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun validateInput(email: String, password: String, isAdmin: Boolean, adminSecret: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email required"
            return false
        }
        if (password.isEmpty()) {
            etPassword.error = "Password required"
            return false
        }
        if (isAdmin && adminSecret != "ADMIN2025!") {
            tilAdminSecretLogin.error = "Invalid admin secret code"
            return false
        } else {
            tilAdminSecretLogin.error = null
        }
        return true
    }

    private fun loginUser(email: String, password: String, isAdmin: Boolean, adminSecret: String) {
        btnLogin.isEnabled = false
        btnLogin.text = "Logging in..."

        lifecycleScope.launch {
            val result = firebaseHelper.loginUser(email, password)

            if (result.isSuccess) {
                Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                if (isAdmin) {
                    startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                } else {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                }
                finish()
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Login Failed: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
                btnLogin.isEnabled = true
                btnLogin.text = "Sign In"
            }
        }
    }

    private fun navigateToDashboard() {
        // Could add automatic role check here if desired
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
