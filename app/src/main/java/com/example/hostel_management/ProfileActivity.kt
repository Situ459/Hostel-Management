package com.example.hostel_management

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var btnAddImage: Button
    private lateinit var tvName: TextView
    private lateinit var tvRegNo: TextView
    private lateinit var tvRoom: TextView
    private lateinit var tvRole: TextView

    private val PICK_IMAGE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imgProfile = findViewById(R.id.imgProfile)
        btnAddImage = findViewById(R.id.btnAddImage)
        tvName = findViewById(R.id.tvName)
        tvRegNo = findViewById(R.id.tvRegNo)
        tvRoom = findViewById(R.id.tvRoom)
        tvRole = findViewById(R.id.tvRole)

        btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val firebaseHelper = FirebaseHelper()
        val userId = firebaseHelper.getCurrentUserId() ?: return

        lifecycleScope.launch {
            val userDetails = firebaseHelper.getUserDetails(userId)
            userDetails?.let {
                tvName.text = "Name: ${it.name}"
                tvRegNo.text = "Reg No: ${it.regNumber}"
                tvRoom.text = "Room: ${it.roomNumber}"
                tvRole.text = "Role: ${it.role.capitalize()}"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imgProfile.setImageURI(imageUri)
            // To persist across launches: upload to Firebase Storage and re-download to set in future.
        }
    }
}
