package com.example.demandmanagementsystem.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.model.MyWorkOrders
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.SortListByDate

class CreatedWorkOrderViewModel(application: Application) : AndroidViewModel(application) {

    init {

        setupSnapshotListener()
    }

    private val reference = FirebaseServiceReference()
    private val sort = SortListByDate()

    val sharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)

    private val createdWorkOrdersList: MutableLiveData<List<MyWorkOrders>> = MutableLiveData()
    private val createdWorkOrderFilterList: MutableLiveData<List<MyWorkOrders>> = MutableLiveData()

    val createdWorkOrderLoading: MutableLiveData<Boolean> = MutableLiveData(false)

    private var createdWorkOrdersListTemp: ArrayList<MyWorkOrders>? = null

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
            .workordersCollection()
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


        val userId = sharedPreferences.getString("userId","")

        createdWorkOrderLoading.value = true

        if (userId != null) {
            reference
                .workordersCollection()
                .get()
                .addOnSuccessListener { docSnapshot ->
                    var createdWorkOrderList = ArrayList<MyWorkOrders>()

                    for (doc in docSnapshot.documents){

                        val workOrder = MyWorkOrders(
                            doc.id,
                            doc.getString("workOrderAssetInformation").toString(),
                            doc.getString("workOrderDate").toString(),
                            doc.getString("workOrderDepartment").toString(),
                            doc.getString("workOrderDescription").toString(),
                            doc.getString("workOrderPersonToDoJob").toString(),
                            doc.getString("workOrderRequestDescription").toString(),
                            doc.getString("workOrderRequestId").toString(),
                            doc.getString("workOrderRequestSubject").toString(),
                            doc.getString("workOrderSubject").toString(),
                            doc.getString("workOrdercreateUserName").toString(),
                            doc.getString("workOrderCase").toString(),
                            doc.getString("selectedWorkOrderUserId").toString(),
                            doc.getString("workOrderRequestType").toString(),
                            doc.getString("workOrderSubDescription").toString(),
                            doc.getString("workOrderUserSubject").toString(),
                            doc.getString("createWorkOrderId").toString(),
                            doc.getString("workOrderType").toString()
                        )

                        if (workOrder.createWorkOrderId == userId){
                            createdWorkOrderList.add(workOrder)
                        }



                    }
                    createdWorkOrdersListTemp = createdWorkOrderList

                    val sortedList = sort.sortWorkOrderListByDate(createdWorkOrderList)

                    createdWorkOrdersList.value = sortedList
                    createdWorkOrderLoading.value = false
                }.addOnFailureListener {
                    createdWorkOrderLoading.value = false
                    Log.e("CreatedWorkOrderViewModel", "fetchData => FireStore Veri Çekme Hatası")
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
                    createdWorkOrderLoading.value = false
                    Log.d("CreatedWorkOrderViewModel", "getData => Kullanıcı bulunamadı")
                }
            }
            .addOnFailureListener { exception ->
                createdWorkOrderLoading.value = false
                Log.e("CreatedWorkOrderViewModel", "getData => Veri çekme hatası: ", exception)
            }
    }
*/
    fun filterList(selectedFilter: String){
        if (createdWorkOrdersListTemp == null){
            return
        }

        val listFilter = ArrayList<MyWorkOrders>()

        for (list in createdWorkOrdersListTemp!!){

            if (list.workOrderCase == selectedFilter){
                listFilter.add(list)
            }

        }

        val sortedList = sort.sortWorkOrderListByDate(listFilter)
        createdWorkOrderFilterList.value = sortedList
    }

    fun getCreatedFilterList(): MutableLiveData<List<MyWorkOrders>> {
        createdWorkOrderLoading.value = false
        return createdWorkOrderFilterList
    }

    fun getCreatedWorkOrdersList(): MutableLiveData<List<MyWorkOrders>> {
        createdWorkOrderLoading.value = false
        return createdWorkOrdersList
    }


}