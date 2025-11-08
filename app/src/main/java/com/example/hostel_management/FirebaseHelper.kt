package com.example.hostel_management

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

class FirebaseHelper {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    companion object {
        const val MAX_OCCUPANCY = 3
    }

    suspend fun getAvailableRooms(): List<RoomInfo> {
        return try {
            val roomsSnapshot = database.child("rooms").get().await()
            val availableRooms = mutableListOf<RoomInfo>()
            for (roomNum in 1..100) {
                val roomKey = "room_$roomNum"
                val occupancy = roomsSnapshot.child(roomKey).child("occupancy")
                    .getValue(Int::class.java) ?: 0
                if (occupancy < MAX_OCCUPANCY) {
                    availableRooms.add(RoomInfo("Room $roomNum", occupancy))
                }
            }
            availableRooms.sortedBy { it.occupancy }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun signUpUser(
        email: String,
        password: String,
        regNumber: String,
        name: String,
        roomNumber: String,
        role: String
    ): Result<String> {
        return try {
            if (roomNumber.isNotEmpty() && roomNumber != "Not Assigned") {
                val isAvailable = checkRoomAvailability(roomNumber)
                if (!isAvailable) {
                    return Result.failure(Exception("Room is now full. Please select another room."))
                }
            }
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return Result.failure(Exception("User creation failed"))
            val userMap = hashMapOf(
                "regNumber" to regNumber,
                "name" to name,
                "email" to email,
                "roomNumber" to roomNumber.ifEmpty { "Not Assigned" },
                "role" to role
            )
            database.child("users").child(userId).setValue(userMap).await()
            if (roomNumber.isNotEmpty() && roomNumber != "Not Assigned") {
                incrementRoomOccupancy(roomNumber, userId)
            }
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun checkRoomAvailability(roomNumber: String): Boolean {
        return try {
            val roomKey = roomNumber.replace(" ", "_").lowercase()
            val snapshot = database.child("rooms").child(roomKey).get().await()
            val occupancy = snapshot.child("occupancy").getValue(Int::class.java) ?: 0
            occupancy < MAX_OCCUPANCY
        } catch (e: Exception) {
            false
        }
    }

    private fun incrementRoomOccupancy(roomNumber: String, userId: String) {
        val roomKey = roomNumber.replace(" ", "_").lowercase()
        val roomRef = database.child("rooms").child(roomKey)
        roomRef.child("occupancy").get().addOnSuccessListener { snapshot ->
            val currentOccupancy = snapshot.getValue(Int::class.java) ?: 0
            roomRef.child("occupancy").setValue(currentOccupancy + 1)
            roomRef.child("members").child(userId).setValue(true)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return Result.failure(Exception("Login failed"))
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserDetails(userId: String): UserDetails? {
        return try {
            val snapshot = database.child("users").child(userId).get().await()
            if (snapshot.exists()) {
                val regNumber = snapshot.child("regNumber").value.toString()
                val name = snapshot.child("name").value.toString()
                val roomNumber = snapshot.child("roomNumber").value.toString()
                val role = snapshot.child("role").value?.toString() ?: "student"
                UserDetails(regNumber, name, roomNumber, role)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllUsersInRoom(roomNumber: String): List<UserDetails> {
        return try {
            val usersSnapshot = database.child("users").get().await()
            val members = mutableListOf<UserDetails>()
            for (userSnapshot in usersSnapshot.children) {
                val regNumber = userSnapshot.child("regNumber").value?.toString() ?: continue
                val name = userSnapshot.child("name").value?.toString() ?: continue
                val room = userSnapshot.child("roomNumber").value?.toString() ?: "Not Assigned"
                val role = userSnapshot.child("role").value?.toString() ?: "student"
                if (room == roomNumber) {
                    members.add(UserDetails(regNumber, name, room, role))
                }
            }
            members
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun submitComplaint(
        regNumber: String,
        name: String,
        roomNumber: String,
        complaintCategories: List<String>,
        complaintText: String
    ): Result<String> {
        return try {
            val complaintId = database.child("complaints").push().key
                ?: return Result.failure(Exception("Failed to generate ID"))
            val complaintMap = hashMapOf(
                "complaintId" to complaintId,
                "regNumber" to regNumber,
                "name" to name,
                "roomNumber" to roomNumber,
                "categories" to complaintCategories,
                "complaintText" to complaintText,
                "timestamp" to ServerValue.TIMESTAMP,
                "status" to "Pending"
            )
            database.child("complaints").child(complaintId).setValue(complaintMap).await()
            Result.success(complaintId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllComplaints(): List<Complaint> {
        return try {
            val snapshot = database.child("complaints").get().await()
            val complaintsList = mutableListOf<Complaint>()
            for (complaintSnapshot in snapshot.children) {
                val complaint = complaintSnapshot.getValue(Complaint::class.java)
                if (complaint != null) complaintsList.add(complaint)
            }
            complaintsList.sortedBy { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateComplaintStatus(complaintId: String, newStatus: String): Boolean {
        return try {
            database.child("complaints").child(complaintId).child("status").setValue(newStatus).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllNotices(): List<Notice> {
        return try {
            val snapshot = database.child("notices").get().await()
            val list = mutableListOf<Notice>()
            for (noticeSnapshot in snapshot.children) {
                val notice = noticeSnapshot.getValue(Notice::class.java)
                if (notice != null) list.add(notice)
            }
            list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addNotice(title: String, content: String, createdBy: String): Result<String> {
        return try {
            val noticeId = database.child("notices").push().key ?: return Result.failure(Exception("No key"))
            val noticeMap = hashMapOf(
                "noticeId" to noticeId,
                "title" to title,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "createdBy" to createdBy
            )
            database.child("notices").child(noticeId).setValue(noticeMap).await()
            Result.success(noticeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNotice(noticeId: String): Boolean {
        return try {
            database.child("notices").child(noticeId).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getFees(session: String): Map<String, Int> {
        return try {
            val snapshot = database.child("fees").child(session).get().await()
            val result = mutableMapOf<String, Int>()
            for (child in snapshot.children) {
                val item = child.key ?: continue
                val amount = child.getValue(Int::class.java) ?: 0
                result[item] = amount
            }
            result
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getUserFeeStatus(userId: String, session: String): Boolean {
        return try {
            val snapshot = database.child("userFeesStatus").child(userId).child(session).get().await()
            snapshot.value as? Boolean ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun markUserFeePaid(userId: String, session: String) {
        try {
            database.child("userFeesStatus").child(userId).child(session).setValue(true).await()
        } catch (e: Exception) {
            // handle if needed
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun logout() = auth.signOut()
    fun isUserLoggedIn(): Boolean = auth.currentUser != null
}

data class UserDetails(
    val regNumber: String,
    val name: String,
    val roomNumber: String,
    val role: String
)

data class RoomInfo(
    val roomNumber: String,
    val occupancy: Int
)

data class Complaint(
    val complaintId: String = "",
    val regNumber: String = "",
    val name: String = "",
    val roomNumber: String = "",
    val categories: List<String> = listOf(),
    val complaintText: String = "",
    val timestamp: Long = 0L,
    var status: String = "Pending"
)

data class Notice(
    val noticeId: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val createdBy: String = ""
)
