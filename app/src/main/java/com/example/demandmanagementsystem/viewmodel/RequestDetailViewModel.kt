package com.example.demandmanagementsystem.viewmodel


import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.model.RequestData
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.RequestUtil

class RequestDetailViewModel(application: Application) : AndroidViewModel(application){
    init {
        setupSnapshotListener(application)
    }
    val sharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)

    private val reference = FirebaseServiceReference()

    private val _requestData = MutableLiveData<RequestData?>()
    val requestData: LiveData<RequestData?> get() = _requestData

    private val _requestDepartmentData = MutableLiveData<String?>()
    val requestDepartmentData: LiveData<String?>
        get() = _requestDepartmentData


    val util = RequestUtil()
    private var alertDialogListener: AlertDialogListener? = null
    fun setAlertDialogListener(listener: AlertDialogListener) {
        alertDialogListener = listener
    }
    private fun setupSnapshotListener(application: Application) {

        val reference = FirebaseServiceReference()
        val sharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
        val uuid = sharedPreferences.getString("userId","")
        if (uuid != null) {
            reference
                .userSigInTokenCollection()
                .document(uuid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("DemandListViewModel", "SnapshotListener error", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {


                        val guide = sharedPreferences.getString("token","")
                        val token = snapshot.getString("token")
                        Log.e("DemandListViewModel", "burada  guide $guide")
                        Log.e("DemandListViewModel", "burada  token $token")

                        if (guide != token){
                            alertDialogListener?.showAlertDialog()
                        }

                    }
                }
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

                    _requestData.value = RequestData(
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
                    )


                } else {
                    _requestData.value = null
                }
            }
            .addOnFailureListener { exception ->
                _requestDepartmentData.value = null
                Log.e("RequestDetailViewModel", "getData => Veri çekme hatası: ", exception)
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
                Toast.makeText(
                    context,
                    "Hata! İş Tamamlanamadı", Toast.LENGTH_SHORT
                ).show()
                Log.e("RequestDetailViewModel", "workCompleted => Hata ")
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
                            Log.e("RequestDetailViewModel", "workCompleted => work => Hata ")
                        }
                        break
                    }
                }
            } .addOnFailureListener { e ->
                Log.e("RequestDetailViewModel", "workCompleted => workorder => Hata ")
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
                Toast.makeText(
                    context,
                    "Hata! Talep Reddedilemedi", Toast.LENGTH_SHORT
                ).show()
                Log.e("RequestDetailViewModel", "requestDenied => Hata ")
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
                Toast.makeText(
                    context,
                    "Hata! İş Reddedilemedi", Toast.LENGTH_SHORT
                ).show()
                Log.e("RequestDetailViewModel", "workDenied => Hata ")
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
                                Log.e("RequestDetailViewModel", "workDenied => work => Hata ")
                            }
                        break
                    }
                }
            } .addOnFailureListener { e ->
                Log.e("RequestDetailViewModel", "workDenied => workOrder => Hata ")
            }


    }






}