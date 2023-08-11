package com.example.demandmanagementsystem.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.databinding.ActivityCreateWorkOrderBinding
import com.example.demandmanagementsystem.model.User
import com.example.demandmanagementsystem.util.WorkOrderUtil
import com.example.demandmanagementsystem.view.DemandListActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class CreateWorkOrderViewModel : ViewModel() {

    private val unit = WorkOrderUtil()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _username = MutableLiveData<String?>()
    val username: MutableLiveData<String?>
        get() = _username

    private val _departmentType = MutableLiveData<String?>()
    val departmentType: MutableLiveData<String?>
        get() = _departmentType

    private val _requestSubject = MutableLiveData<String?>()
    val requestSubject: MutableLiveData<String?>
        get() = _requestSubject

    private val _requestDescription = MutableLiveData<String?>()
    val requestDescription: MutableLiveData<String?>
        get() = _requestDescription

    private val _selectedUser = MutableLiveData<User?>()
    val selectedUser: MutableLiveData<User?>
        get() = _selectedUser

    private val _userNameList = MutableLiveData<List<String>>()
    val userNameList: LiveData<List<String>>
        get() = _userNameList

    private val _selectedUserId = MutableLiveData<String>()
    val selectedUserId: LiveData<String>
        get() = _selectedUserId

    private val _departmentUsersList = MutableLiveData<List<User>>()
    val departmentUsersList: LiveData<List<User>>
        get() = _departmentUsersList


    fun loadRequestData(requestID: String) {
        val requestCollectionRef = firestore.collection("requests")
        requestCollectionRef.document(requestID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val workOrderRequestSubject = documentSnapshot.getString("requestSubject")
                    val workOrderRequestDescription = documentSnapshot.getString("requestDescription")
                    _requestSubject.value = workOrderRequestSubject
                    _requestDescription.value = workOrderRequestDescription
                } else {
                    _requestSubject.value = "Talep bulunamadı"
                    _requestDescription.value = ""
                }
            }
            .addOnFailureListener { exception ->
                _requestSubject.value = "Veri çekme hatası"
                _requestDescription.value = ""
            }
    }

    fun loadUserData() {
        val usersCollectionRef = firestore.collection("users")
        val user = auth.currentUser
        val userId = user!!.uid

        usersCollectionRef.document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val username = documentSnapshot.getString("name")
                    val departmentType = documentSnapshot.getString("deparmentType")
                    _username.value = username
                    _departmentType.value = departmentType
                } else {
                    _username.value = "Kullanıcı bulunamadı"
                    _departmentType.value = ""
                }
            }
            .addOnFailureListener { exception ->
                _username.value = "Veri çekme hatası"
                _departmentType.value = ""
            }
    }



    fun getUsersDataSpinner() {
        val usersTypeCollectionRef = FirebaseFirestore.getInstance().collection("users")
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user!!.uid

        usersTypeCollectionRef.document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val userDepartmentType = documentSnapshot.getString("deparmentType").toString()

                usersTypeCollectionRef.get()
                    .addOnSuccessListener { documentsnapshot ->
                        val departmentUsersList = mutableListOf<User>()
                        for (document in documentsnapshot.documents) {
                            val departmentType = document.getString("deparmentType").toString()

                            if ((document.id != userId) && (departmentType == userDepartmentType)) {
                                val user = User(document.getString("name").toString(), document.id)
                                departmentUsersList.add(user)
                            }
                        }
                        _userNameList.value = departmentUsersList.map { it.userName }
                        if (departmentUsersList.isNotEmpty()) {
                            _selectedUserId.value = departmentUsersList[0].userId
                            _selectedUser.value = departmentUsersList[0] // Seçili kullanıcıyı güncelle
                        }

                        _departmentUsersList.value = departmentUsersList
                    }
                    .addOnFailureListener {
                        Log.e("CreateWorkOrderViewModel", "FireStore Veri Çekme Hatası")
                    }
            }
            .addOnFailureListener {
                Log.e("CreateWorkOrderViewModel", "FireStore Veri Çekme Hatası")
            }
    }

    // Bu metodu kullanıcıyı id'sine göre bulmak için kullanacağız
    fun getUserById(userId: String): User? {
        return departmentUsersList.value?.firstOrNull { it.userId == userId }
    }

    // Kullanıcıyı seçtiğimizde çağrılacak metot
    fun setSelectedUser(userId: String?) {
        val user = getUserById(userId.orEmpty())
        _selectedUser.value = user
    }


    fun wordOrderCreate(context: Context,binding: ActivityCreateWorkOrderBinding
                        ,selectedUserId: String?){


            val calendar = Calendar.getInstance()

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // Ay 0 ile başlar, bu yüzden 1 ekliyoruz
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)


            val workordersCollectionRef = firestore.collection("workorders")
            val selectedWorkOrderUserId = selectedUserId
            val workOrderRequestId = binding.textWorkOrderRequestId.text.toString()
            val workOrderPersonToDoJob = binding.textWorkOrderPersonToDoJob.text.toString()
            val workOrdercreateUserName = binding.textWorkOrderCreateUserName.text.toString()
            val workOrderDepartment = binding.textWorkOrderDepartment.text.toString()
            val workOrderRequestSubject = binding.textWorkOrderRequestSubject.text.toString()
            val workOrderRequestDescription = binding.textWorkOrderRequestDescription.text.toString()
            val workOrderSubject = binding.textWorkOrderSubject.text.toString()
            val workOrderDescription = binding.textWorkOrderDescription.text.toString()
            val workOrderAssetInformation = binding.textWorkOrderAssetInformation.text.toString()
            val workOrderCase = unit.assignedToPerson
            val workOrderDate = "$hour:$minute-$day/$month/$year"

            val workOrder = hashMapOf(
                "workOrderRequestId" to workOrderRequestId,
                "workOrderPersonToDoJob" to workOrderPersonToDoJob,
                "workOrdercreateUserName" to workOrdercreateUserName,
                "workOrderDepartment" to workOrderDepartment,
                "workOrderRequestSubject" to workOrderRequestSubject,
                "workOrderRequestDescription" to workOrderRequestDescription,
                "workOrderSubject" to workOrderSubject,
                "workOrderDescription" to workOrderDescription,
                "workOrderAssetInformation" to workOrderAssetInformation,
                "workOrderDate" to workOrderDate,
                "selectedWorkOrderUserId" to selectedWorkOrderUserId,
                "workOrderCase" to workOrderCase
            )

            workordersCollectionRef
                .document()
                .set(workOrder)
                .addOnSuccessListener {
                    Log.d("CreateWorkOrderActivity", "Firestore'a iş emri başarıyla eklendi.")
                    Toast.makeText(context,"İş Emri Gönderildi", Toast.LENGTH_SHORT).show()

                    val intent = Intent(context, DemandListActivity::class.java)
                    context.startActivity(intent)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP


                }
                .addOnFailureListener { e ->
                    Toast.makeText(context,"Hata", Toast.LENGTH_SHORT).show()
                }

            val requestCollectionRef = firestore.collection("requests")

            val updateData = hashMapOf<String, Any>(
                "requestCase" to "İş Yapacak Kişiye Atandı"
            )

            requestCollectionRef
                .document(workOrderRequestId)
                .update(updateData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Güncelleme başarılı!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->

                    Toast.makeText(context, "Hata: $e", Toast.LENGTH_SHORT).show()
                }

        }


    }


