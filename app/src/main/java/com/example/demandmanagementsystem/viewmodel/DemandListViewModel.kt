package com.example.demandmanagementsystem.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.model.Requests
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.SortListByDate
import com.example.demandmanagementsystem.view.AddPersonActivity
import com.example.demandmanagementsystem.view.CreateRequestActivity
import com.example.demandmanagementsystem.view.CreateWorkOrderActivity
import com.example.demandmanagementsystem.view.CreatedRequestsActivity
import com.example.demandmanagementsystem.view.CreatedWorkOrderActivity
import com.example.demandmanagementsystem.view.MainActivity
import com.example.demandmanagementsystem.view.MyWorkOrdersActivity

class DemandListViewModel : ViewModel() {

    private val reference = FirebaseServiceReference()
    private val sort = SortListByDate()

    private val requestList: MutableLiveData<List<Requests>> = MutableLiveData()
    private val requestFilterList: MutableLiveData<List<Requests>> = MutableLiveData()
    val requestLoading: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _username: MutableLiveData<String?> = MutableLiveData()
    private val _departmentType: MutableLiveData<String?> = MutableLiveData()

    val username: MutableLiveData<String?>
        get() = _username


    val departmentType: MutableLiveData<String?>
        get() = _departmentType

    private val _authorityType: MutableLiveData<String?> = MutableLiveData()

    val authorityType: MutableLiveData<String?>
        get() = _authorityType

    private  var requestListTemp: ArrayList<Requests>? = null

    fun fetchData() {

        val user = reference.getFirebaseAuth().currentUser
        val userId = user!!.uid

        requestLoading.value = true

        reference.usersCollection().document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {

                    reference.requestsCollection()
                        .get()
                        .addOnSuccessListener { documentsnapshot ->
                            var requestsList = ArrayList<Requests>()

                            for (document in documentsnapshot.documents) {
                                val requests = Requests(
                                    document.id,
                                    document.getString("requestCase").toString(),
                                    document.getString("requestContactNumber").toString(),
                                    document.getString("requestDate").toString(),
                                    document.getString("requestDepartment").toString(),
                                    document.getString("requestDescription").toString(),
                                    document.getString("requestName").toString(),
                                    document.getString("requestSendDepartment").toString(),
                                    document.getString("requestSendID").toString(),
                                    document.getString("requestSubject").toString(),
                                    document.getString("requestType").toString()
                                )

                                if (departmentType.value == requests.requestSendDepartment) {
                                    requestsList.add(requests)
                                }
                            }
                            requestListTemp = requestsList
                            val sortedList = sort.sortRequestsListByDate(requestsList)

                            requestList.value = sortedList
                            requestLoading.value = false
                        }
                        .addOnFailureListener {
                            requestLoading.value = false
                            Log.e("Firestore", "FireStore Veri Çekme Hatası")
                        }
                } else {
                    requestLoading.value = false
                    Log.d("Firestore", "Kullanıcı bulunamadı")
                }
            }
            .addOnFailureListener { exception ->
                requestLoading.value = false
                Log.e("Firestore", "Veri çekme hatası: ", exception)
            }
    }


    fun getData(){

        val user = reference.getFirebaseAuth().currentUser
        val userId = user!!.uid

        reference.usersCollection().document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {

                    val username = documentSnapshot.getString("name")
                    _username.value = username

                    val departmentType = documentSnapshot.getString("deparmentType")
                    _departmentType.value = departmentType

                    val authorityType = documentSnapshot.getString("authorityType")
                    _authorityType.value = authorityType
                } else {
                    requestLoading.value = false
                    Log.d("Firestore", "Kullanıcı bulunamadı")
                }
            }
            .addOnFailureListener { exception ->
                requestLoading.value = false
                Log.e("Firestore", "Veri çekme hatası: ", exception)
            }

    }

    fun filterList(selectedFilter: String) {
        if (requestListTemp == null) {
            // requestListTemp null ise, hata oluşmasını önlemek için işlem yapmayın
            return
        }

        val listFilter = ArrayList<Requests>()
        for (list in requestListTemp!!){
            if (list.requestCase == selectedFilter){
                listFilter.add(list)
            }
        }
        val sortedList = sort.sortRequestsListByDate(listFilter)
        requestFilterList.value = sortedList

    }


    fun getRequestFilterList(): MutableLiveData<List<Requests>>{

        requestLoading.value = false
        return requestFilterList

    }


    fun getRequestList(): MutableLiveData<List<Requests>> {
        requestLoading.value = false
        return requestList
    }

    fun onCreateRequestClick(context: Context) {
        val intent = Intent(context, CreateRequestActivity::class.java)
        context.startActivity(intent)
    }

    fun onCreateWorkOrderClick(context: Context) {
        val intent = Intent(context, CreateWorkOrderActivity::class.java)
        context.startActivity(intent)
    }

    fun onLogOutClick(context: Context) {
        reference.getFirebaseAuth().signOut()
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun onCreatedRequestsClick(context: Context) {
        val intent = Intent(context, CreatedRequestsActivity::class.java)
        context.startActivity(intent)
    }

    fun onAddPersonClick(context: Context) {
        val intent = Intent(context, AddPersonActivity::class.java)
        context.startActivity(intent)
    }
    fun onMyWorkOrders(context: Context) {
        val intent = Intent(context, MyWorkOrdersActivity::class.java)
        context.startActivity(intent)
    }

    fun onCreatedWorkOrdersClick(context: Context) {
        val intent = Intent(context, CreatedWorkOrderActivity::class.java)
        context.startActivity(intent)
    }

}