package com.example.demandmanagementsystem.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.demandmanagementsystem.model.UserData
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
class GetUserSaveData {
    private val reference = FirebaseServiceReference()

    suspend fun getUserSaveData(userId: String, context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)

        return try {
            val documentSnapshot = reference
                .usersCollection()
                .document(userId)
                .get()
                .await()

            if (documentSnapshot.exists()) {

                val tcIdentityNo = documentSnapshot.getString("tcIdentityNo").toString()
                val email = documentSnapshot.getString("email").toString()
                val name = documentSnapshot.getString("name").toString()
                val password = tcIdentityNo.substring(0, 6)
                val telNo = documentSnapshot.getString("telNo").toString()
                val authorityType = documentSnapshot.getString("authorityType").toString()
                val departmentType = documentSnapshot.getString("deparmentType").toString()

                sharedPreferences.edit().apply {
                    putString("userId", userId)
                    putString("tcIdentityNo", tcIdentityNo)
                    putString("email", email)
                    putString("name", name)
                    putString("password", password)
                    putString("telNo", telNo)
                    putString("authorityType", authorityType)
                    putString("departmentType", departmentType)
                    apply()
                }

                true
            } else {
                Log.d("GetUserData", "fetchData => Kullanıcı bulunamadı")
                false
            }
        } catch (e: Exception) {
            Log.e("GetUserData", "Hata: ${e.localizedMessage}")
            false
        }
    }
}
