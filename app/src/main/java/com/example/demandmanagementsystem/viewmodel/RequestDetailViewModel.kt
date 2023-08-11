package com.example.demandmanagementsystem.viewmodel


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.model.RequestData
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.RequestUtil

class RequestDetailViewModel: ViewModel(){

    private val reference = FirebaseServiceReference()

    private val _requestData = MutableLiveData<RequestData?>()
    val requestData: LiveData<RequestData?> get() = _requestData

    private val _requestCaseData = MutableLiveData<String?>()
    val requestCaseData: LiveData<String?>
        get() = _requestCaseData

    private val _requestDepartmentData = MutableLiveData<String?>()
    val requestDepartmentData: LiveData<String?>
        get() = _requestDepartmentData

    private val _userDepartmentData = MutableLiveData<String?>()
    val userDepartmentData: LiveData<String?>
        get() = _userDepartmentData

    val util = RequestUtil()

    fun getAuthorityType(callback: (String?) -> (Unit)) {
        val user = reference.getFirebaseAuth().currentUser
        val userId = user?.uid

        if (userId != null) {
            reference.usersCollection().document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val authorityType = documentSnapshot.getString("authorityType")
                    val departmentType = documentSnapshot.getString("deparmentType")
                    _userDepartmentData.value = departmentType
                    callback.invoke(authorityType)
                }
                .addOnFailureListener { exception ->
                    callback.invoke(null)
                }
        } else {
            callback.invoke(null)
        }
    }


    fun getData(requestID: String) {

        reference.requestsCollection().document(requestID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val requestDetailId = documentSnapshot.id
                    val requestDetailName = documentSnapshot.getString("requestName")
                    val requestDetailDepartment = documentSnapshot.getString("requestDepartment")
                    val requestDetailSendDepartment = documentSnapshot.getString("requestSendDepartment")
                    val requestDetailSubject = documentSnapshot.getString("requestSubject")
                    val requestDetailDescription = documentSnapshot.getString("requestDescription")
                    val requestDetailDate = documentSnapshot.getString("requestDate")
                    val requestDetailCase = documentSnapshot.getString("requestCase")
                    val requestDetailType = documentSnapshot.getString("requestType")
                    val requestWorkOrderSubDescription = documentSnapshot.getString("workOrderSubDescription")
                    val requestWorkOrderUserSubject = documentSnapshot.getString("workOrderUserSubject")
                    val requestDenied = documentSnapshot.getString("requestDenied")

                    _requestCaseData.value = requestDetailCase
                    _requestDepartmentData.value = requestDetailSendDepartment

                    _requestData.setValue(RequestData(
                        requestDetailId,
                        requestDetailName,
                        requestDetailDepartment,
                        requestDetailSendDepartment,
                        requestDetailSubject,
                        requestDetailDescription,
                        requestDetailDate,
                        requestDetailCase,
                        requestDetailType,
                        requestWorkOrderUserSubject,
                        requestWorkOrderSubDescription,
                        requestDenied
                    ))
                } else {
                    _requestCaseData.value = null
                    _requestDepartmentData.value = null
                    _requestData.value = null
                }
            }
            .addOnFailureListener { exception ->
                _requestData.value = null
                _requestCaseData.value = null
                _requestDepartmentData.value = null
                Log.e("Firestore", "Veri çekme hatası: ", exception)
            }
    }


    fun workCompleted(requestID: String, context: Context){

        val updateData = hashMapOf<String, Any>(
            "requestCase" to util.completed
        )

        val updateWorkOrderData = hashMapOf<String, Any>(
            "workOrderCase" to util.completed
        )

        reference.requestsCollection()
            .document(requestID)
            .update(updateData)
            .addOnSuccessListener { documentSnapshot ->

                Toast.makeText(
                    context,
                    "İş Tamamlandı", Toast.LENGTH_SHORT
                ).show()

            }.addOnFailureListener { e ->
                Log.e("RequestDetailViewModel", "Hata ")
            }

        reference.workordersCollection()
            .get()
            .addOnSuccessListener { documentSnapshot ->
                for (document in documentSnapshot.documents){
                    val workOrderRequestId = document.getString("workOrderRequestId").toString()
                    if (workOrderRequestId == requestID){

                        reference.workordersCollection()
                            .document(document.id)
                            .update(updateWorkOrderData)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener { e ->
                            Log.e("RequestDetailViewModel", "Hata ")
                        }
                        break
                    }
                }
            } .addOnFailureListener { e ->
                Log.e("RequestDetailViewModel", "Hata ")
            }

    }

    fun requestDenied(requestID: String, context: Context,dataReceived: String){

        val updateData = hashMapOf<String, Any>(
            "requestCase" to util.deniedRequest,
            "requestDenied" to dataReceived
        )

        reference.requestsCollection()
            .document(requestID)
            .update(updateData)
            .addOnSuccessListener { documentSnapshot ->

                Toast.makeText(
                    context,
                    "Talep Reddedildi", Toast.LENGTH_SHORT
                ).show()

            }.addOnFailureListener { e ->
                Log.e("RequestDetailViewModel", "Hata ")
            }

    }

    fun workDenied(requestID: String, context: Context){

        val updateData = hashMapOf<String, Any>(
            "requestCase" to util.deniedWork
        )

        val updateWorkOrderData = hashMapOf<String, Any>(
            "workOrderCase" to util.deniedWork
        )

        reference.requestsCollection()
            .document(requestID)
            .update(updateData)
            .addOnSuccessListener { documentSnapshot ->

                Toast.makeText(
                    context,
                    "İş Reddedildi", Toast.LENGTH_SHORT
                ).show()

            }.addOnFailureListener { e ->
                Log.e("RequestDetailViewModel", "Hata ")
            }

        reference.workordersCollection()
            .get()
            .addOnSuccessListener { documentSnapshot ->
                for (document in documentSnapshot.documents){
                    val workOrderRequestId = document.getString("workOrderRequestId").toString()
                    if (workOrderRequestId == requestID){
                        reference.workordersCollection()
                            .document(document.id)
                            .update(updateWorkOrderData)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener { e ->
                                Log.e("RequestDetailViewModel", "Hata ")
                            }
                        break
                    }
                }
            } .addOnFailureListener { e ->
                Log.e("RequestDetailViewModel", "Hata ")
            }


    }






}