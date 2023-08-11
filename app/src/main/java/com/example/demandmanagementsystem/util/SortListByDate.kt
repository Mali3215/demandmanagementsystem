package com.example.demandmanagementsystem.util

import com.example.demandmanagementsystem.model.MyWorkOrders
import com.example.demandmanagementsystem.model.Requests
import java.text.SimpleDateFormat
import java.util.Locale

class SortListByDate {

    fun sortRequestsListByDate(requestList: List<Requests>): List<Requests> {
        val dateFormat = SimpleDateFormat("HH:mm-dd/MM/yyyy", Locale.getDefault())

        return requestList.sortedByDescending { request ->
            dateFormat.parse(request.requestDate)
        }
    }

    fun sortWorkOrderListByDate(myWorkOrderList: List<MyWorkOrders>): List<MyWorkOrders> {
        val dateFormat = SimpleDateFormat("HH:mm-dd/MM/yyyy", Locale.getDefault())

        return myWorkOrderList.sortedByDescending { myWorkOrder ->
            dateFormat.parse(myWorkOrder.workOrderDate)
        }
    }

}