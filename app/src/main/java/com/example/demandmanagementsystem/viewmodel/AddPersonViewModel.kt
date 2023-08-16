package com.example.demandmanagementsystem.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.model.UserData
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.view.DemandListActivity

class AddPersonViewModel : ViewModel() {

    private val reference = FirebaseServiceReference()
    val departmentTypeList = MutableLiveData<List<String>?>()
    val typeOfStaffList = MutableLiveData<List<String>?>()

    init {
        fetchDepartmentTypes()
        fetchTypeOfStaffList()
    }

    private fun fetchDepartmentTypes() {
            reference.departmentTypeCollection()
            .get()
            .addOnSuccessListener { documentsnapshot ->
                val departments = mutableListOf<String>()
                for (document in documentsnapshot.documents) {
                    val departmentType = document.getString("departmentType")
                    departmentType?.let { departments.add(it) }
                }
                departmentTypeList.postValue(departments)
            }
            .addOnFailureListener { exception ->
                departmentTypeList.postValue(null)
                Log.e("AddPersonViewModel", "fetchDepartmentTypes => FireStore Veri Çekme Hatası: $exception")
            }
    }

    private fun fetchTypeOfStaffList() {
        reference.employeeCollection()
            .get()
            .addOnSuccessListener { documentsnapshot ->
                val staffTypes = mutableListOf<String>()
                for (document in documentsnapshot.documents) {
                    val typeOfStaff = document.getString("typeOfStaff")
                    typeOfStaff?.let { staffTypes.add(it) }
                }
                typeOfStaffList.postValue(staffTypes)
            }
            .addOnFailureListener { exception ->
                typeOfStaffList.postValue(null)
                Log.e("AddPersonViewModel", "fetchTypeOfStaffList => FireStore Veri Çekme Hatası: $exception")
            }
    }

    fun addUser(userData: UserData,context: Context) {

        val userMap = hashMapOf(
            "tcIdentityNo" to userData.tcIdentityNo,
            "email" to userData.email,
            "name" to userData.name,
            "telNo" to userData.telNo,
            "authorityType" to userData.authorityType,
            "deparmentType" to userData.departmentType
        )

        val user = reference.getFirebaseAuth().currentUser!!.uid

        reference.getFirebaseAuth()
            .createUserWithEmailAndPassword(userData.email,userData.password)
            .addOnSuccessListener {
                val userId = reference.getFirebaseAuth().currentUser?.uid
                if (userId != null) {
                    reference.usersCollection()
                        .document(userId)
                        .set(userMap)
                        .addOnSuccessListener {

                            reference.getFirebaseAuth().signOut()

                            getUser(user, context)

                           Toast.makeText(
                               context,
                               "Kullanıcı Eklendi",Toast.LENGTH_SHORT
                           ).show()
                            Log.d("AddPersonViewModel", "addUser => Firestore'a kayıt başarıyla eklendi.")
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Hata!! Kullanıcı Eklenmedi",Toast.LENGTH_SHORT
                            ).show()
                            Log.e("AddPersonViewModel", "addUser => Firestore'a kayıt ekleme hatası: $e")
                        }
                }
            }

    }


    fun getUser(userId: String, context: Context){

      reference.usersCollection()
          .document(userId)
          .get()
          .addOnSuccessListener { documentSnapshot ->

              val email = documentSnapshot.getString("email").toString()
              val password = documentSnapshot.getString("tcIdentityNo")?.substring(0, 6)

              if (password != null) {
                  reference.getFirebaseAuth().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                      if (task.isSuccessful) {

                          val intent = Intent(context,
                              DemandListActivity::class.java)
                          intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                          context.startActivity(intent)
                      }

                  }.addOnFailureListener { exception ->
                      Log.e("AddPersonViewModel","getUser => ${exception.localizedMessage}")
                  }
              }


          }.addOnFailureListener {
              Log.e("AddPersonViewModel","getUser => ${it.localizedMessage}")
          }

    }


}