package com.example.demandmanagementsystem.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.model.Requests
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.SortListByDate

class CreatedRequestsViewModel(application: Application) : AndroidViewModel(application) {


    init {

        setupSnapshotListener()
    }

    private val reference = FirebaseServiceReference()
    private val sort = SortListByDate()

    val sharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)

    private val createdRequestsList: MutableLiveData<List<Requests>> = MutableLiveData()
    private val createdRequestsFilterList: MutableLiveData<List<Requests>> = MutableLiveData()
    val  createdRequestsLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    private  var createdRequestsListTemp: ArrayList<Requests>? = null


    private val _username: MutableLiveData<String?> = MutableLiveData()
    private val _departmentType: MutableLiveData<String?> = MutableLiveData()

    val username: MutableLiveData<String?>
        get() = _username

    val departmentType: MutableLiveData<String?>
        get() = _departmentType

    private val _authorityType: MutableLiveData<String?> = MutableLiveData()

    val authorityType: MutableLiveData<String?>
        get() = _authorityType

    private fun setupSnapshotListener() {

        val reference = FirebaseServiceReference()

        reference
            .requestsCollection()
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DemandListViewModel", "SnapshotListener error", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    fetchData()
                    getData()
                }
            }

    }

    fun fetchData() {
        val userId = sharedPreferences.getString("userId",null)
        createdRequestsLoading.value = true

        if (userId != null) {
            reference.usersCollection().document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {

                        reference.requestsCollection()
                            .get()
                            .addOnSuccessListener { documentsnapshot ->
                                var createdRequestList = ArrayList<Requests>()

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
                                        document.getString("requestSendId").toString(),
                                        document.getString("requestSubject").toString(),
                                        document.getString("requestType").toString()
                                    )

                                    if (requests.requestSendID == userId) {
                                        createdRequestList.add(requests)
                                    }
                                }
                                createdRequestsListTemp = createdRequestList
                                val sortedList = sort.sortRequestsListByDate(createdRequestList)

                                createdRequestsList.value = sortedList
                                createdRequestsLoading.value = false
                            }
                            .addOnFailureListener {
                                createdRequestsLoading.value = false
                                Log.e("CreatedRequestViewModel", "fetchData => FireStore Veri Çekme Hatası")
                            }
                    } else {
                        createdRequestsLoading.value = false
                        Log.d("CreatedRequestViewModel", "fetchData => Kullanıcı bulunamadı")
                    }
                }
                .addOnFailureListener { exception ->
                    createdRequestsLoading.value = false
                    Log.e("CreatedRequestViewModel", "fetchData => Veri çekme hatası: ", exception)
                }
        }
    }
    fun getData(){
        _username.value = sharedPreferences.getString("name","")
        _departmentType.value =  sharedPreferences.getString("departmentType","")
        _authorityType.value =  sharedPreferences.getString("authorityType","")
    }
/*
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
                    createdRequestsLoading.value = false
                    Log.d("CreatedRequestViewModel", "getData => Kullanıcı bulunamadı")
                }
            }
            .addOnFailureListener { exception ->
                createdRequestsLoading.value = false
                Log.e("CreatedRequestViewModel", "getData => Veri çekme hatası: ", exception)
            }

    }
*/
    fun filterList(selectedFilter: String){
        if (createdRequestsListTemp == null) {
            return
        }

        val listFilter = ArrayList<Requests>()
        for (list in createdRequestsListTemp!!){
            if (list.requestCase == selectedFilter){
                listFilter.add(list)
            }
        }
        val sortedList = sort.sortRequestsListByDate(listFilter)
        createdRequestsFilterList.value = sortedList
    }

    fun getCreatedFilterList(): MutableLiveData<List<Requests>>{
        createdRequestsLoading.value = false
        return createdRequestsFilterList
    }


    fun getCreatedRequestsList(): MutableLiveData<List<Requests>> {
        createdRequestsLoading.value = false
        return  createdRequestsList
    }

}