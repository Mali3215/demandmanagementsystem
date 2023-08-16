package com.example.demandmanagementsystem.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.databinding.ActivityCreateRequestBinding
import com.example.demandmanagementsystem.model.CreateRequest
import com.example.demandmanagementsystem.model.JobDetails
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.CurrentDateTime
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.view.DemandListActivity

class CreateRequestViewModel: ViewModel() {

    private val reference = FirebaseServiceReference()
    private val dateTime = CurrentDateTime()
    val departmentTypeList = MutableLiveData<List<String>?>()
    private val _requestCreationStatus = MutableLiveData<Boolean>()
    private val util = RequestUtil()

    private val jobDetailsList: MutableLiveData<List<JobDetails>?> = MutableLiveData()
    private var jobDetailsListTemp: ArrayList<JobDetails>? = null
    private var jobDetailsJobTypeList: ArrayList<String>? = null
    private var jobDetailsSubTypeList: ArrayList<String>? = null


    private val _username: MutableLiveData<String?> = MutableLiveData()
    private val _departmentType: MutableLiveData<String?> = MutableLiveData()

    val usernames: MutableLiveData<String?>
        get() = _username


    val departmentTypes: MutableLiveData<String?>
        get() = _departmentType

    init {
        fetchDepartmentTypes()
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
                Log.e("CreateRequestViewModel", "fetchDepartmentTypes => Firestore Veri Çekme Hatası: $exception")
            }
    }

    fun requestFill(binding: ActivityCreateRequestBinding){

        val user = reference.getFirebaseAuth().currentUser
        val userId = user!!.uid

        val textRequestName = binding.textRequestUserName
        val textRequestDepartment = binding.textRequestDepartment
        val textRequestTelNo = binding.textRequestContactNumber

        reference.usersCollection().document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {

                    val username = documentSnapshot.getString("name")

                    val departmentType = documentSnapshot.getString("deparmentType")

                    val telNo = documentSnapshot.getString("telNo")
                    if (username != null) {
                        usernames.value = username
                        departmentTypes.value = departmentType

                        textRequestName.setText(username)
                        textRequestDepartment.setText(departmentType)
                        textRequestTelNo.setText(telNo)
                    }
                } else {
                    Log.d("CreateRequestViewModel", "fetchDepartmentTypes => Kullanıcı bulunamadı")
                }

            }
            .addOnFailureListener { exception ->
                Log.e("CreateRequestViewModel", "fetchDepartmentTypes => Veri çekme hatası: ", exception)
            }
    }

    fun createRequest(request: CreateRequest, context: Context) {
        val user = reference.getFirebaseAuth().currentUser
        val userId = user?.uid

        if (userId == null) {

            _requestCreationStatus.value = false
            return
        }

        val requestMap = hashMapOf(
            "requestSendId" to userId,
            "requestType" to request.requestType,
            "requestName" to request.requestName,
            "requestDepartment" to request.requestDepartment,
            "requestSendDepartment" to request.requestSendDepartment,
            "requestSubject" to request.requestSubject,
            "requestDescription" to request.requestDescription,
            "requestContactNumber" to request.requestContactNumber,
            "requestDate" to dateTime.getCurrentDateTime(),
            "requestCase" to util.newRequest
        )


       reference.requestsCollection()
            .add(requestMap)
            .addOnSuccessListener {

                val intent = Intent(context,
                    DemandListActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                context.startActivity(intent)
                Toast.makeText(
                    context,
                    "Talep Oluşturuldu", Toast.LENGTH_SHORT
                ).show()

                Log.d("CreateRequestViewModel", "fetchDepartmentTypes => Firestore'a talep başarıyla eklendi.")
                _requestCreationStatus.value = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Hata! Talep Oluşturulamadı", Toast.LENGTH_SHORT
                ).show()
                Log.e("CreateRequestViewModel", "fetchDepartmentTypes => Firestore'a talep ekleme hatası: $e")

                _requestCreationStatus.value = false
            }
    }

    fun getJobDetails(){
        reference
            .jobDetailsCollection()
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val tempList = ArrayList<JobDetails>() // Yeni bir liste oluştur
                for (document in documentSnapshot){
                    val jobDetails = JobDetails(
                        document.id,
                        document.getString("jobType").toString(),
                        document.getString("departmentType").toString(),
                        document.get("businessSubtype") as? List<String>

                    )

                    tempList.add(jobDetails) // Geçici listeye ekle
                }

                jobDetailsListTemp = tempList // Geçici listeyi asıl listeye ata
                jobDetailsList.value = jobDetailsListTemp

            }.addOnFailureListener {
                Log.e("CreateRequestViewModel","getJobDetails =>  fonksiyonunda hata")
            }
    }

    fun spinnerDataJobType(departmenType: String) {
        jobDetailsJobTypeList = ArrayList() // Liste başlatılıyor

        if (jobDetailsListTemp == null) {
            // requestListTemp null ise, hata oluşmasını önlemek için işlem yapmayın
            return
        }

        for (jobDetail in jobDetailsListTemp!!){
            if (jobDetail.departmentType == departmenType){
                jobDetailsJobTypeList!!.add(jobDetail.jobType)
            }
        }
    }

    fun spinnerDataSubType(jobType: String){
        jobDetailsSubTypeList = ArrayList() // Liste başlatılıyor
        val tempList = ArrayList<String>()

        if (jobDetailsListTemp == null) {
            // requestListTemp null ise, hata oluşmasını önlemek için işlem yapmayın
            return
        }

        for (jobDetail in jobDetailsListTemp!!){
            if (jobDetail.jobType == jobType){

                for (subType in jobDetail.businessSubtype!!){
                    jobDetailsSubTypeList!!.add(subType)
                }



            }
        }
    }

    fun getJobDetailsSubTypeList(): List<String>? {
        return jobDetailsSubTypeList
    }


    fun getJobDetailsJobTypeList(): List<String>?{
        return jobDetailsJobTypeList
    }


}