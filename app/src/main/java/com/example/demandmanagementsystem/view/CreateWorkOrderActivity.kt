package com.example.demandmanagementsystem.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.databinding.ActivityCreateWorkOrderBinding
import com.example.demandmanagementsystem.model.JobDetails
import com.example.demandmanagementsystem.model.User
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.util.WorkOrderUtil
import com.example.demandmanagementsystem.viewmodel.CreateWorkOrderViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CreateWorkOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateWorkOrderBinding
    private lateinit var auth: FirebaseAuth;
    private lateinit var firestore: FirebaseFirestore
    private var requestID: String? = null
    private val departmentUsersList = ArrayList<User>()
    private var userDepartmentType : String? = null
    private lateinit var viewModel: CreateWorkOrderViewModel
    private var userId: String? = null
    private var selectedUserId: String? = null
    private val util = RequestUtil()
    private val utilWorkOrder = WorkOrderUtil()
    private lateinit var getSpinnerRequestDataAdapter: ArrayAdapter<String>
    private lateinit var arrayRequestInfo: ArrayList<String>
    private var spinnerDepartmentType = ""
    private lateinit var jobDetailsListTemp: ArrayList<JobDetails>
    private lateinit var spinnerWorkOrderTypeList: ArrayList<String>
    private lateinit var spinnerWorkOrderSubTypeList: ArrayList<String>
    private lateinit var spinnerWorkTypeAdapter: ArrayAdapter<String>
    private lateinit var spinnerWorkSubTypeAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@CreateWorkOrderActivity
            , R.layout.activity_create_work_order)

        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
        jobDetailsListTemp = ArrayList()
        binding.toolbarWorkOrder.title = "WorkOrder"
        setSupportActionBar(binding.toolbarWorkOrder)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel = ViewModelProvider(this).get(CreateWorkOrderViewModel::class.java)


        val incomingData = intent.getIntExtra(util.intentRequestDetail,0)

        if (incomingData == 1){
            getSpinner()
            requestID = intent.getStringExtra(util.intentRequestId)
            binding.layoutWorkOrderDescription.visibility = View.GONE
            binding.layoutWorkOrderSubject.visibility = View.GONE
            binding.layoutWorkOrderType.visibility = View.GONE
            loadingData(requestID!!)

        }else{

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    jobDetailsListTemp = getJobDetails()
                    val departmentType = loadUserData()
                    getSpinner()
                    spinnerDataJobType(departmentType)

                    spinnerWorkTypeAdapter = ArrayAdapter(this@CreateWorkOrderActivity, android.R.layout.simple_list_item_1,
                        android.R.id.text1, spinnerWorkOrderTypeList
                    )

                    binding.spinnerWorkOrderType.adapter = spinnerWorkTypeAdapter

                    binding.spinnerWorkOrderType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                            binding.textWorkOrderType.setText(spinnerWorkOrderTypeList[p2])
                            spinnerDataSubType(spinnerWorkOrderTypeList[p2])
                        }
                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }

                    }

                    binding.layoutWorkOrderType.visibility = View.VISIBLE
                    binding.layoutWorkOrderRequestType.visibility = View.GONE
                    binding.layoutWorkOrderRequestId.visibility = View.GONE
                    binding.layoutWorkOrderRequestDescription.visibility = View.GONE
                    binding.layoutWorkOrderRequestSubject.visibility = View.GONE

                    // ...
                } catch (e: Exception) {
                    // Hata yönetimi burada yapılır
                    Log.e("CreateWorkOrderActivity", "CoroutineScope => Hata: ${e.message}")
                }
            }
        }
    }

    suspend fun getJobDetails(): ArrayList<JobDetails> {
        return suspendCoroutine { continuation ->
            firestore.collection("jobdetails")
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val tempList = ArrayList<JobDetails>()
                    for (document in documentSnapshot) {
                        val jobDetails = JobDetails(
                            document.id,
                            document.getString("jobType").toString(),
                            document.getString("departmentType").toString(),
                            document.get("businessSubtype") as? List<String>
                        )
                        tempList.add(jobDetails)
                    }
                    continuation.resume(tempList)
                }
                .addOnFailureListener {
                    Log.e("CreateWorkOrderActivity", "getJobDetails => fonksiyonunda hata")
                    continuation.resumeWithException(it)
                }
        }
    }

    suspend fun loadUserData(): String {
        return suspendCoroutine { continuation ->
            val usersCollectionRef = firestore.collection("users")

            val user = auth.currentUser
            val userId = user!!.uid

            usersCollectionRef.document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val username = documentSnapshot.getString("name")
                        val departmentType = documentSnapshot.getString("deparmentType")



                        if (username != null) {

                            binding.textWorkOrderPersonToDoJob.setText(username)
                            binding.textWorkOrderDepartment.setText(departmentType)
                            continuation.resume(departmentType.toString())
                        }
                    } else {
                        Log.d("CreateWorkOrderActivity", "loadUserData => Kullanıcı bulunamadı")
                        continuation.resume("")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("CreateWorkOrderActivity", "loadUserData => Veri çekme hatası: ", exception)
                    continuation.resumeWithException(exception)
                }
        }
    }

    fun spinnerDataJobType(departmenType: String) {
        spinnerWorkOrderTypeList = ArrayList() // Liste başlatılıyor

        if (jobDetailsListTemp == null) {
            // requestListTemp null ise, hata oluşmasını önlemek için işlem yapmayın
            return
        }

        for (jobDetail in jobDetailsListTemp!!){
            if (jobDetail.departmentType == departmenType){
                spinnerWorkOrderTypeList!!.add(jobDetail.jobType)
            }
        }
    }

    fun spinnerDataSubType(jobType: String){
        spinnerWorkOrderSubTypeList = ArrayList() // Liste başlatılıyor
        val tempList = ArrayList<String>()

        if (jobDetailsListTemp == null) {
            // requestListTemp null ise, hata oluşmasını önlemek için işlem yapmayın
            return
        }

        for (jobDetail in jobDetailsListTemp!!){
            if (jobDetail.jobType == jobType){

                for (subType in jobDetail.businessSubtype!!){
                    spinnerWorkOrderSubTypeList!!.add(subType)
                }

            }
        }

        spinnerWorkSubTypeAdapter = ArrayAdapter(this@CreateWorkOrderActivity, android.R.layout.simple_list_item_1,
            android.R.id.text1, spinnerWorkOrderSubTypeList
        )

        binding.spinnerCreateWorkOrderRequestSubject.adapter = spinnerWorkSubTypeAdapter

        binding.spinnerCreateWorkOrderRequestSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                binding.textWorkOrderSubject.setText(spinnerWorkOrderSubTypeList[p2])
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }


        }

    }

    fun getSpinnerRequestData(spinnerList: List<String>){

        getSpinnerRequestDataAdapter = ArrayAdapter(this@CreateWorkOrderActivity, android.R.layout.simple_list_item_1,
            android.R.id.text1, spinnerList
        )

        binding.spinnerCreateWorkOrderRequestSubject.adapter = getSpinnerRequestDataAdapter

        binding.spinnerCreateWorkOrderRequestSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, indeks: Int, p3: Long) {
                binding.textWorkOrderSubject.setText(spinnerList[indeks])

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }


    }
    /*
                        arrayRequestInfo.add(workOrderRequestType)
                           arrayRequestInfo.add(workOrderRequestSendDepartmen)
                        */
    fun getDataSpinnerRequest(completion: (List<String>) -> Unit) {
        firestore.collection("jobdetails")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val resultList = mutableListOf<String>()
                resultList.add("Seçiniz")
                for (document in documentSnapshot) {
                    val jobDetails = JobDetails(
                        document.id,
                        document.getString("jobType").toString(),
                        document.getString("departmentType").toString(),
                        document.get("businessSubtype") as? List<String>
                    )

                    if ((jobDetails.departmentType == arrayRequestInfo[1]) &&
                        (jobDetails.jobType == arrayRequestInfo[0])) {

                        val list = jobDetails.businessSubtype
                        resultList.addAll(list ?: emptyList())
                    }
                }

                completion(resultList)
            }
            .addOnFailureListener {
                Log.e("CreateWorkOrderActivity", "getDataSpinnerRequest => getDataSpinnerRequest")
                completion(emptyList())
            }
    }




    fun getSpinner() {
        getUsersDataSpinner {
            // Firestore verileri çekildiğinde spinner'ı güncelleyin.
            val userNameList: List<String> = departmentUsersList.map { it.userName }
            val spinnerDataAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1
                , userNameList)
            binding.spinnerCreateWorkOrder.adapter = spinnerDataAdapter

            binding.spinnerCreateWorkOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, indeks: Int, p3: Long) {
                    selectedUserId = departmentUsersList[indeks].userId
                    binding.textWorkOrderCreateUserName.setText(departmentUsersList[indeks].userName)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }


    }

    fun getUsersDataSpinner(callback: () -> Unit) {
        val usersTypeCollectionRef = firestore.collection("users")
        val user = auth.currentUser
        userId = user!!.uid

        usersTypeCollectionRef.document(userId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                userDepartmentType = documentSnapshot.getString("deparmentType").toString()
                spinnerDepartmentType = userDepartmentType.toString()
                usersTypeCollectionRef.get()
                    .addOnSuccessListener { documentsnapshot ->
                        departmentUsersList.clear()
                        departmentUsersList.add(0,User("Seçiniz","0"))
                        for (document in documentsnapshot.documents) {
                            val departmentType = document.getString("deparmentType").toString()

                            if ((document.id != userId) && (departmentType == userDepartmentType)) {
                                val user = User(document.getString("name").toString(), document.id)
                                departmentUsersList.add(user)
                            }
                        }
                        callback.invoke()
                    }
                    .addOnFailureListener {
                        Log.e("CreateWorkOrderActivity", "getUsersDataSpinner => FireStore Veri Çekme Hatası")
                        callback.invoke()
                    }
            }
            .addOnFailureListener {
                Log.e("CreateWorkOrderActivity", "getUsersDataSpinner => FireStore Veri Çekme Hatası")
                callback.invoke()
            }
    }
    fun  loadingData(requestID: String){

        arrayRequestInfo = ArrayList()

        val usersCollectionRef = firestore.collection("users")
        val requestCollectionRef = firestore.collection("requests")

        val textWorkOrderRequestId = findViewById<TextView>(R.id.textWorkOrderRequestId)
        val textWorkOrderRequestSubject = findViewById<TextView>(R.id.textWorkOrderRequestSubject)
        val textWorkOrderRequestDescription = findViewById<TextView>(R.id.textWorkOrderRequestDescription)
        val textWorkOrderRequestType = findViewById<TextView>(R.id.textWorkOrderRequestType)

        if((requestID != null) && (requestID != "")){
            textWorkOrderRequestId.text = requestID  // talep ID

            requestCollectionRef.document(requestID)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documentSnapshot = task.result
                        val workOrderRequestSendDepartmen = documentSnapshot.getString("requestSendDepartment")
                        val workOrderRequestType = documentSnapshot.getString("requestType")
                        val workOrderRequestSubject = documentSnapshot.getString("requestSubject")
                        val workOrderRequestDescription = documentSnapshot.getString("requestDescription")

                        if ((workOrderRequestType != null) && (workOrderRequestSendDepartmen != null)) {
                            arrayRequestInfo.add(workOrderRequestType)
                            arrayRequestInfo.add(workOrderRequestSendDepartmen)

                            getDataSpinnerRequest { list ->

                                getSpinnerRequestData(list)
                            }

                        }

                        textWorkOrderRequestSubject.text = workOrderRequestSubject
                        textWorkOrderRequestDescription.text = workOrderRequestDescription
                        textWorkOrderRequestType.text = workOrderRequestType

                    } else {

                        Log.d("CreateWorkOrderActivity", "loadingData => Talep bulunamadı")
                    }
                }
                .addOnFailureListener { exception ->

                    Log.e("CreateWorkOrderActivity", "loadingData => Veri çekme hatası: ", exception)
                }
        }

        val user = auth.currentUser
        val userId = user!!.uid

        val name = findViewById<TextView>(R.id.textWorkOrderPersonToDoJob)
        val department = findViewById<TextView>(R.id.textWorkOrderDepartment)


        usersCollectionRef.document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {

                    val username = documentSnapshot.getString("name")
                    val departmentType = documentSnapshot.getString("deparmentType")
                    if (username != null) {
                        name.text = username
                        department.text = departmentType

                    }
                } else {

                    Log.d("CreateWorkOrderActivity", "loadingData => userscollec =>  Kullanıcı bulunamadı")
                }
            }
            .addOnFailureListener { exception ->

                Log.e("CreateWorkOrderActivity", "loadingData => usercollec => Veri çekme hatası: ", exception)
            }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.request_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reject -> {

                binding.textWorkOrderSubject.setText("")
                binding.textWorkOrderDescription.setText("")
                binding.textWorkOrderAssetInformation.setText("")
                binding.textWorkOrderCreateUserName.setText("")
                true
            }
            R.id.workOrderCreate -> {
                val user = auth.currentUser
                val userId = user!!.uid
                val alerDialog = AlertDialog.Builder(this@CreateWorkOrderActivity)

                alerDialog.setMessage("İş Emri Oluşturulsun Mu?")
                alerDialog.setPositiveButton("Evet"){ dialogInterface, i ->

                    val incomingData = intent.getStringExtra(utilWorkOrder.intentWorkOrderId)

                    if (incomingData == null){
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
                        val workOrderCase = util.assignedToPerson
                        val workOrderDate = "$hour:$minute-$day/$month/$year"
                        val workOrderRequestType =binding.textWorkOrderRequestType.text.toString()
                        val workOrderType = binding.textWorkOrderType.text.toString()
                        val createWorkOrderId = userId



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
                            "workOrderCase" to workOrderCase,
                            "workOrderRequestType" to workOrderRequestType,
                            "workOrderType" to workOrderType,
                            "createWorkOrderId" to createWorkOrderId
                        )

                        workordersCollectionRef
                            .document()
                            .set(workOrder)
                            .addOnSuccessListener {
                                Log.d("CreateWorkOrderActivity", "onOptionsItemSelected => Firestore'a iş emri başarıyla eklendi.")
                                Toast.makeText(this@CreateWorkOrderActivity,"İş Emri Gönderildi", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@CreateWorkOrderActivity, DemandListActivity::class.java)
                                startActivity(intent)
                                finish()

                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@CreateWorkOrderActivity,"Hata! İş Emri Gönderilemedi", Toast.LENGTH_SHORT).show()
                                Log.e("CreateWorkOrderActivity","")
                            }

                        val requestCollectionRef = firestore.collection("requests")

                        val updateData = hashMapOf<String, Any>(
                            "requestCase" to "İş Yapacak Kişiye Atandı"
                        )

                        if (workOrderRequestId != "") {
                            requestCollectionRef
                                .document(workOrderRequestId!!)
                                .update(updateData)
                                .addOnSuccessListener {
                                    Toast.makeText(this@CreateWorkOrderActivity, "İş Yapacak Kişiye Atandı!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("CreateWorkOrderActivity","onOptionsItemSelected => requestCollectionRef")
                                    Toast.makeText(this@CreateWorkOrderActivity, "Hata! İş Yapacak Kişiye Atanamadı", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }else {
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
                        val workOrderCase = util.assignedToPerson
                        val workOrderDate = "$hour:$minute-$day/$month/$year"
                        val workOrderRequestType =binding.textWorkOrderRequestType.text.toString()
                        val workOrderType = binding.textWorkOrderType.text.toString()
                        val createWorkOrderId = userId

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
                            "workOrderCase" to workOrderCase,
                            "workOrderRequestType" to workOrderRequestType,
                            "workOrderType" to workOrderType,
                            "createWorkOrderId" to createWorkOrderId
                        )

                        workordersCollectionRef
                            .document(incomingData)
                            .set(workOrder)
                            .addOnSuccessListener {
                                Log.d("CreateWorkOrderActivity", "onOptionsItemSelected => Firestore'a iş emri başarıyla eklendi.")
                                Toast.makeText(this@CreateWorkOrderActivity,"İş Emri Gönderildi", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@CreateWorkOrderActivity, DemandListActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@CreateWorkOrderActivity,"Hata! İş Emri Gönderilemedi", Toast.LENGTH_SHORT).show()
                                Log.e("CreateWorkOrderActivity","onOptionsItemSelected => WorkOrderRef")
                            }

                        val requestCollectionRef = firestore.collection("requests")

                        val updateData = hashMapOf<String, Any>(
                            "requestCase" to "İş Yapacak Kişiye Atandı"
                        )

                        if (workOrderRequestId != "") {
                            requestCollectionRef
                                .document(workOrderRequestId!!)
                                .update(updateData)
                                .addOnSuccessListener {
                                    Toast.makeText(this@CreateWorkOrderActivity, "İş Yapacak Kişiye Atandı!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("CreateWorkOrderActivity"," onOptionsItemSelected =< reqCollec")
                                    Toast.makeText(this@CreateWorkOrderActivity, "Hata: İş Yapacak Kişiye Atanamadı", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }

                alerDialog.setNegativeButton("Hayır"){ dialogInterface, i ->
                }
                alerDialog.create().show()
                true
            }
            android.R.id.home -> {
                onBackPressed() // Geri dönme işlemini yapar
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}