package com.example.demandmanagementsystem.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.databinding.ActivityMyWorkOrderDetailBinding
import com.example.demandmanagementsystem.model.JobDetails
import com.example.demandmanagementsystem.model.MyWorkOrders
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.WorkOrderUtil
import com.example.demandmanagementsystem.view.DemandListActivity
import com.example.demandmanagementsystem.view.MyWorkOrderDetailActivity
import com.example.demandmanagementsystem.view.MyWorkOrdersActivity


class MyWorkOrderDetailViewModel(application: Application) : AndroidViewModel(application) {
    init {
        setupSnapshotListener(application)
    }
    private val reference = FirebaseServiceReference()
    val sharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
    private val _workOrderData = MutableLiveData<MyWorkOrders?>()
    val workOrderData: LiveData<MyWorkOrders?> get() = _workOrderData
    private val _workOrderUserSubject = MutableLiveData<String>()
    val workOrderUserSubject: LiveData<String?> get() = _workOrderUserSubject

    private val util = WorkOrderUtil()

    private lateinit var arrayRequestInfo: ArrayList<String>

    private val _userDepartmentData = MutableLiveData<String?>()
    val userDepartmentData: LiveData<String?>
        get() = _userDepartmentData

    private val _userId = MutableLiveData<String?>()
    val userId: LiveData<String?>
        get() = _userId

    fun getAuthorityType(callback: (String?) -> (Unit)) {
        val currentUserId = sharedPreferences.getString("userId","")

        callback.invoke(currentUserId)
    }
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


    fun getData(workOrderID: String) {

        arrayRequestInfo = ArrayList()

        reference.workordersCollection().document(workOrderID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val workOrderDetailId = documentSnapshot.id
                    val workOrderDetailAssetInformation = documentSnapshot.getString("workOrderAssetInformation")
                    val workOrderDetailDate = documentSnapshot.getString("workOrderDate")
                    val workOrderDetailDepartment = documentSnapshot.getString("workOrderDepartment")
                    val workOrderDetailDescription = documentSnapshot.getString("workOrderDescription")
                    val workOrderDetailPersonToDoJob = documentSnapshot.getString("workOrderPersonToDoJob")
                    val workOrderDetailRequestDescription = documentSnapshot.getString("workOrderRequestDescription")
                    val workOrderDetailRequestId = documentSnapshot.getString("workOrderRequestId")
                    val workOrderDetailRequestSubject = documentSnapshot.getString("workOrderRequestSubject")
                    val workOrderDetailSubject = documentSnapshot.getString("workOrderSubject")
                    val workOrderDetailCreateUserName = documentSnapshot.getString("workOrdercreateUserName")
                    val workOrderDetailCase = documentSnapshot.getString("workOrderCase")
                    val selectedWorkOrderUserId = documentSnapshot.getString("selectedWorkOrderUserId")
                    val workOrderRequestType = documentSnapshot.getString("workOrderRequestType")
                    val workOrderSubDescription = documentSnapshot.getString("workOrderSubDescription")
                    val workOrderUserSubject = documentSnapshot.getString("workOrderUserSubject")
                    val createWorkOrderId = documentSnapshot.getString("createWorkOrderId")
                    val workOrderType = documentSnapshot.getString("workOrderType")

                    _workOrderData.value = MyWorkOrders(
                        workOrderDetailId,
                        workOrderDetailAssetInformation,
                        workOrderDetailDate,
                        workOrderDetailDepartment,
                        workOrderDetailDescription,
                        workOrderDetailPersonToDoJob,
                        workOrderDetailRequestDescription,
                        workOrderDetailRequestId,
                        workOrderDetailRequestSubject,
                        workOrderDetailSubject,
                        workOrderDetailCreateUserName,
                        workOrderDetailCase,
                        selectedWorkOrderUserId,
                        workOrderRequestType,
                        workOrderSubDescription,
                        workOrderUserSubject,
                        createWorkOrderId,
                        workOrderType
                    )
                    _workOrderUserSubject.value =  _workOrderData.value!!.workOrderUserSubject.toString()


                    if ((workOrderRequestType != null) && (workOrderDetailDepartment != null)) {
                        if (workOrderRequestType != ""){
                            arrayRequestInfo.add(workOrderRequestType)
                        }else if(workOrderType != null){
                            if (workOrderType != ""){
                                arrayRequestInfo.add(workOrderType)
                            }

                        }

                        arrayRequestInfo.add(workOrderDetailDepartment)

                    }


                } else {
                    _workOrderData.value = null
                }
            }
            .addOnFailureListener { exception ->
                _workOrderData.value = null
                Log.e("MyWorkOrderDetailViewModel", "getData => Veri çekme hatası: ", exception)
            }
    }

    fun menuComletedUpdate(context: Context, tempKind: Int,binding: ActivityMyWorkOrderDetailBinding){

        val updateData = hashMapOf<String, Any>(
            "workOrderCase" to util.waitingForApproval,
            "workOrderSubDescription" to binding.textWorkOrderSubDescription.text.toString(),
            "workOrderUserSubject" to binding.textWorkOrderUserSubject.text.toString()
        )

        val updateRequestData = hashMapOf<String, Any>(
            "requestCase" to util.waitingForApproval,
            "workOrderSubDescription" to binding.textWorkOrderSubDescription.text.toString(),
            "workOrderUserSubject" to binding.textWorkOrderUserSubject.text.toString()
        )

        // -> talepli iş emri
        if (tempKind == util.tempKindRequest){
            reference.requestsCollection()
                .document(_workOrderData.value!!.workOrderRequestId!!)
                .update(updateRequestData)
                .addOnSuccessListener {

                    val intent = Intent(context,
                        DemandListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    context.startActivity(intent)
                    Toast.makeText(
                        context,
                        "İş Onaya Gönderildi", Toast.LENGTH_SHORT
                    ).show()

                    Log.e("MyWorkOrderDetailViewModel", "menuComletedUpdate => Güncellendi ")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Hata! İş Onaya Gönderilemedi", Toast.LENGTH_SHORT
                    ).show()
                    Log.e("MyWorkOrderDetailViewModel", "menuComletedUpdate => Hata ")
                }
        }


        reference.workordersCollection()
            .document(_workOrderData.value!!.workOrderID!!)
            .update(updateData)
            .addOnSuccessListener {
                val intent = Intent(context,
                    DemandListActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
                Toast.makeText(
                    context,
                    "İş Onaya Gönderildi", Toast.LENGTH_SHORT
                ).show()

                Log.e("MyWorkOrderDetailViewModel", "menuComletedUpdate => Güncellendi ")
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Hata! İş Onaya Gönderilemedi", Toast.LENGTH_SHORT
                ).show()
                Log.e("MyWorkOrderDetailViewModel", "menuComletedUpdate => Hata ")
            }

    }

    fun menuDeniedUpdate(context: Context, binding: ActivityMyWorkOrderDetailBinding,tempKind: Int){

        val updateData = hashMapOf<String, Any>(
            "workOrderCase" to util.deniedWork,
            "workOrderSubDescription" to binding.textWorkOrderSubDescription.text.toString(),
            "workOrderUserSubject" to binding.textWorkOrderUserSubject.text.toString()
        )
        val updateRequestData = hashMapOf<String, Any>(
            "requestCase" to util.deniedWork,
            "workOrderSubDescription" to binding.textWorkOrderSubDescription.text.toString(),
            "workOrderUserSubject" to binding.textWorkOrderUserSubject.text.toString()
        )
        if (tempKind == util.tempKindRequest){
            reference.requestsCollection()
                .document(_workOrderData.value!!.workOrderRequestId!!)
                .update(updateRequestData)
                .addOnSuccessListener {

                    val intent = Intent(context,
                        DemandListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    context.startActivity(intent)
                    Toast.makeText(
                        context,
                        "İş Reddedildi", Toast.LENGTH_SHORT
                    ).show()
                    Log.e("MyWorkOrderDetailViewModel", "menuDeniedUpdate => Güncellendi ")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Hata! İş Reddedilemedi", Toast.LENGTH_SHORT
                    ).show()
                    Log.e("MyWorkOrderDetailViewModel", "menuDeniedUpdate => Hata ")
                }
        }


        reference.workordersCollection()
            .document(_workOrderData.value!!.workOrderID!!)
            .update(updateData)
            .addOnSuccessListener {

                val intent = Intent(context,
                    DemandListActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
                Toast.makeText(
                    context,
                    "İş Reddedildi", Toast.LENGTH_SHORT
                ).show()
                Log.e("MyWorkOrderDetailViewModel", "menuDeniedUpdate => work => Güncellendi ")
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Hata! İş Reddedilemedi", Toast.LENGTH_SHORT
                ).show()
                Log.e("MyWorkOrderDetailViewModel", "menuDeniedUpdate => work => Hata ")
            }

    }

    /*
                      arrayRequestInfo.add(workOrderRequestType)
                         arrayRequestInfo.add(workOrderRequestSendDepartmen)
                      */
    fun getDataSpinnerRequest(completion: (List<String>) -> Unit) {
        reference
            .jobDetailsCollection()
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val resultList = mutableListOf<String>()

                for (document in documentSnapshot) {
                    val jobDetails = JobDetails(
                        document.id,
                        document.getString("jobType").toString(),
                        document.getString("departmentType").toString(),
                        document.get("businessSubtype") as? List<String>
                    )
                    if (arrayRequestInfo.size > 1){
                        if ((jobDetails.departmentType == arrayRequestInfo[1]) &&
                            (jobDetails.jobType == arrayRequestInfo[0])) {

                            val list = jobDetails.businessSubtype
                            resultList.addAll(list ?: emptyList())
                        }
                    }

                }

                completion(resultList)
            }
            .addOnFailureListener {
                Log.e("MyWorkOrderDetailViewModel", "getDataSpinnerRequest")
                completion(emptyList())
            }
    }

    fun updateStartRequest(myWorkOrderID: String, context: Context,binding: ActivityMyWorkOrderDetailBinding){

        val updateData = hashMapOf<String, Any>(
            "workOrderCase" to util.activityProcessed,
            "workOrderSubDescription" to binding.textWorkOrderSubDescription.text.toString(),
            "workOrderUserSubject" to binding.textWorkOrderUserSubject.text.toString()
        )

        reference.workordersCollection()
            .document(myWorkOrderID)
            .update(updateData)
            .addOnSuccessListener {

                val intent = Intent(context,MyWorkOrderDetailActivity::class.java)
                intent.putExtra(util.intentWorkOrderId, myWorkOrderID)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
                Toast.makeText(
                    context,
                    "Aktiviteye Başlandı", Toast.LENGTH_SHORT
                ).show()
                Log.e("MyWorkOrderDetailViewModel", "updateStartRequest => Güncellendi ")
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Aktiviteye Başlanamadı", Toast.LENGTH_SHORT
                ).show()
                Log.e("MyWorkOrderDetailViewModel", "updateStartRequest => Hata ")
            }
    }

    fun startedActivity(context: Context,binding: ActivityMyWorkOrderDetailBinding,
                        myWorkOrderID: String){

        binding.layoutWorkOrderUserSubject.visibility = View.VISIBLE
        binding.layoutWorkOrderSubDescription.visibility = View.VISIBLE
        updateStartRequest(myWorkOrderID,context,binding)


    }

    fun workOrderSave(
        binding: ActivityMyWorkOrderDetailBinding,
        context: Context,
        myWorkOrderID: String
    ){


        val updateData =  hashMapOf(
            "workOrderRequestId" to _workOrderData.value!!.workOrderRequestId,
            "workOrderPersonToDoJob" to _workOrderData.value!!.workOrderPersonToDoJob,
            "workOrdercreateUserName" to _workOrderData.value!!.workOrdercreateUserName,
            "workOrderDepartment" to _workOrderData.value!!.workOrderDepartment,
            "workOrderRequestSubject" to _workOrderData.value!!.workOrderRequestSubject,
            "workOrderRequestDescription" to _workOrderData.value!!.workOrderRequestDescription,
            "workOrderSubject" to _workOrderData.value!!.workOrderSubject,
            "workOrderDescription" to _workOrderData.value!!.workOrderDescription,
            "workOrderAssetInformation" to _workOrderData.value!!.workOrderAssetInformation,
            "workOrderDate" to _workOrderData.value!!.workOrderDate,
            "selectedWorkOrderUserId" to _workOrderData.value!!.selectedWorkOrderUserId,
            "workOrderCase" to _workOrderData.value!!.workOrderCase,
            "workOrderRequestType" to _workOrderData.value!!.workOrderRequestType,
            "workOrderSubDescription" to binding.textWorkOrderSubDescription.text.toString(),
            "workOrderUserSubject" to binding.textWorkOrderUserSubject.text.toString(),
            "createWorkOrderId" to _workOrderData.value!!.createWorkOrderId,
            "workOrderType" to _workOrderData.value!!.workOrderType
        )

        reference
            .workordersCollection()
            .document(myWorkOrderID)
            .set(updateData)
            .addOnSuccessListener {
                val intent = Intent(context,MyWorkOrdersActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
                Toast.makeText(context, "İş Emri Kaydedildi" , Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Hata! İş Emri Kaydedilemedi" , Toast.LENGTH_SHORT).show()
                Log.e("MyWorkOrderDetailViewModel", "workOrderSave => güncellenmedi")
            }



    }

    fun jobReturn( binding: ActivityMyWorkOrderDetailBinding,
                     context: Context,
                     myWorkOrderID: String){


        val updateData =  hashMapOf(
            "workOrderRequestId" to _workOrderData.value!!.workOrderRequestId,
            "workOrderPersonToDoJob" to _workOrderData.value!!.workOrderPersonToDoJob,
            "workOrdercreateUserName" to _workOrderData.value!!.workOrdercreateUserName,
            "workOrderDepartment" to _workOrderData.value!!.workOrderDepartment,
            "workOrderRequestSubject" to _workOrderData.value!!.workOrderRequestSubject,
            "workOrderRequestDescription" to _workOrderData.value!!.workOrderRequestDescription,
            "workOrderSubject" to _workOrderData.value!!.workOrderSubject,
            "workOrderDescription" to _workOrderData.value!!.workOrderDescription,
            "workOrderAssetInformation" to _workOrderData.value!!.workOrderAssetInformation,
            "workOrderDate" to _workOrderData.value!!.workOrderDate,
            "selectedWorkOrderUserId" to _workOrderData.value!!.selectedWorkOrderUserId,
            "workOrderCase" to util.jobReturn,
            "workOrderRequestType" to _workOrderData.value!!.workOrderRequestType,
            "workOrderSubDescription" to binding.textWorkOrderSubDescription.text.toString(),
            "workOrderUserSubject" to binding.textWorkOrderUserSubject.text.toString(),
            "createWorkOrderId" to _workOrderData.value!!.createWorkOrderId
        )

        reference
            .workordersCollection()
            .document(myWorkOrderID)
            .set(updateData)
            .addOnSuccessListener {
                val intent = Intent(context,MyWorkOrdersActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
                Toast.makeText(context, "İş İade Edildi" , Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Hata! İş İade Edilemedi" , Toast.LENGTH_SHORT).show()
                Log.e("MyWorkOrderDetailViewModel", "jobReturn => güncellenmedi")
            }

    }

    fun workCompleted(myWorkOrderID: String, context: Context,requestID: String){

        val updateWorkOrderData = hashMapOf<String, Any>(
            "workOrderCase" to util.completed
        )

        reference.workordersCollection()
            .document(myWorkOrderID)
            .update(updateWorkOrderData)
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
                Log.e("RequestDetailViewModel", "workCompleted => work Hata ")
            }

        val updateData = hashMapOf<String, Any>(
            "requestCase" to util.completed
        )
        Log.e("MyWorkOrderDetailViewModel", "=>$requestID")
        if (requestID != ""){
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
                    Log.e("MyWorkOrderDetailViewModel", "workCompleted => ref => Hata ")
                }
        }



    }


}

/*
   fun getAuthorityType(callback: (String?) -> (Unit)) {
        val user = reference.getFirebaseAuth().currentUser
        val currentUserId = user?.uid

        callback.invoke(currentUserId)


        /*if (userId != null) {
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
        }*/
    }

 */