package com.example.demandmanagementsystem.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.model.MyWorkOrders
import com.example.demandmanagementsystem.model.Requests
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.SortListByDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

class MyWorkOrdersViewModel: ViewModel() {

    private val reference = FirebaseServiceReference()
    private val sort = SortListByDate()

    private val myWorkOrderList: MutableLiveData<List<MyWorkOrders>> = MutableLiveData()
    private val myWorkOrderFilterList: MutableLiveData<List<MyWorkOrders>> = MutableLiveData()
    val myWorkOrderLoading: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _username: MutableLiveData<String?> = MutableLiveData()
    private val _departmentType: MutableLiveData<String?> = MutableLiveData()
    private  var myWorkOrderListTemp: ArrayList<MyWorkOrders>? = null

    val username: MutableLiveData<String?>
        get() = _username

    val departmentType: MutableLiveData<String?>
        get() = _departmentType

    private val _authorityType: MutableLiveData<String?> = MutableLiveData()

    val authorityType: MutableLiveData<String?>
        get() = _authorityType

    fun fetchData() {
        val user = reference.getFirebaseAuth().currentUser
        val userId = user!!.uid

        myWorkOrderLoading.value = true

        reference.usersCollection().document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {

                  reference.workordersCollection()
                        .get()
                        .addOnSuccessListener { documentsnapshot ->
                            var myWorkOrdersList = ArrayList<MyWorkOrders>()

                            for (document in documentsnapshot.documents) {
                                val myWorkOrder = MyWorkOrders(
                                    document.id,
                                    document.getString("workOrderAssetInformation").toString(),
                                    document.getString("workOrderDate").toString(),
                                    document.getString("workOrderDepartment").toString(),
                                    document.getString("workOrderDescription").toString(),
                                    document.getString("workOrderPersonToDoJob").toString(),
                                    document.getString("workOrderRequestDescription").toString(),
                                    document.getString("workOrderRequestId").toString(),
                                    document.getString("workOrderRequestSubject").toString(),
                                    document.getString("workOrderSubject").toString(),
                                    document.getString("workOrdercreateUserName").toString(),
                                    document.getString("workOrderCase").toString(),
                                    document.getString("selectedWorkOrderUserId").toString(),
                                    document.getString("workOrderRequestType").toString(),
                                    document.getString("workOrderSubDescription").toString(),
                                    document.getString("workOrderUserSubject").toString(),
                                    document.getString("createWorkOrderId").toString()
                                )

                                if (((departmentType.value == myWorkOrder.workOrderDepartment)
                                    && (myWorkOrder.selectedWorkOrderUserId == userId)) || (myWorkOrder.selectedWorkOrderUserId == userId)) {
                                    myWorkOrdersList.add(myWorkOrder)
                                }
                            }

                            myWorkOrderListTemp = myWorkOrdersList

                            val sortedList = sort.sortWorkOrderListByDate(myWorkOrdersList)

                            myWorkOrderList.value = sortedList
                            myWorkOrderLoading.value = false
                        }
                        .addOnFailureListener {
                            myWorkOrderLoading.value = false
                            Log.e("Firestore", "FireStore Veri Çekme Hatası")
                        }
                } else {
                    myWorkOrderLoading.value = false
                    Log.d("Firestore", "Kullanıcı bulunamadı")
                }
            }
            .addOnFailureListener { exception ->
                myWorkOrderLoading.value = false
                Log.e("Firestore", "Veri çekme hatası: ", exception)
            }
    }
    fun filterList(selectedFilter: String) {
        if (myWorkOrderListTemp == null) {
            return
        }

        val listFilter = ArrayList<MyWorkOrders>()
        for (list in myWorkOrderListTemp!!){
            if (list.workOrderCase == selectedFilter){
                listFilter.add(list)
            }
        }
        val sortedList = sort.sortWorkOrderListByDate(listFilter)
        myWorkOrderFilterList.value = sortedList

    }


    fun getWorkOrderFilterList(): MutableLiveData<List<MyWorkOrders>>{

        myWorkOrderLoading.value = false
        return myWorkOrderFilterList

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
                    myWorkOrderLoading.value = false
                    Log.d("Firestore", "Kullanıcı bulunamadı")
                }
            }
            .addOnFailureListener { exception ->
                myWorkOrderLoading.value = false
                Log.e("Firestore", "Veri çekme hatası: ", exception)
            }

    }

    fun getMyOrderWorkList(): MutableLiveData<List<MyWorkOrders>> {
        myWorkOrderLoading.value = false
        return myWorkOrderList
    }



}