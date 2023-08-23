package com.example.demandmanagementsystem.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.demandmanagementsystem.model.RequestData
import com.example.demandmanagementsystem.model.UserData
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.view.DemandListActivity
import com.google.firebase.auth.EmailAuthProvider
import java.util.UUID

class UserProfileInfoViewModel(application: Application) : AndroidViewModel(application) {
    val sharedPreferences: SharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
    private val reference = FirebaseServiceReference()
    fun getData(callbacks: (UserData) -> Unit) {
        val tc = sharedPreferences.getString("tcIdentityNo", "")
        val email = sharedPreferences.getString("email", "")
        val name = sharedPreferences.getString("name", "")
        val password = sharedPreferences.getString("password", "")
        val tel = sharedPreferences.getString("telNo", "")
        val authorityType = sharedPreferences.getString("authorityType", "")
        val departmentType = sharedPreferences.getString("departmentType", "")

        if ((tc != null) && (email != null) && (name != null) && (email != null) && (password != null) && (tel != null) && (authorityType != null) && (departmentType != null)) {
        val user =   UserData(
            tc,email,name,password,tel,authorityType,departmentType)
            callbacks.invoke(user)
        }


    }

    fun resetPassword(newPassword: String,context: Context){

        val email = sharedPreferences.getString("email", "")
        val password = sharedPreferences.getString("password", "")



        if((email != null) && (password != null)){

            reference
                .getFirebaseAuth()
                .signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val auth = reference.getFirebaseAuth().currentUser
                        if (auth != null) {
                            auth.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        // Şifre güncelleme başarılı
                                        Toast.makeText(context, "Şifre başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
                                        reference.getFirebaseAuth().signOut()
                                    } else {
                                        // Şifre güncelleme başarısız
                                        Toast.makeText(context, "Şifre güncelleme başarısız. Hata: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                        }
                }



        }

    }

}