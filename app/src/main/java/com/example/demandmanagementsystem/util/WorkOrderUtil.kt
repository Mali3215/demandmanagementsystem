package com.example.demandmanagementsystem.util

import android.graphics.Color
import android.util.Log

class WorkOrderUtil {

    val intentWorkOrderId = "WORKORDER_ID"

    val assignedToPerson = "İş Yapacak Kişiye Atandı"

    val waitingForApproval = "Onay Bekliyor"

    val completed = "Tamamlandı"

    val deniedWork = "İş Reddedildi"

    val activityStart = "Aktiviteye Başla"  // bu butona basıldığında 2 adet sekme açılacak
                                            // 1. sekme iş alt türü
                                            // 2. sekme iş emri sonuç bilgileri

    val activityProcessed = "Aktivite İşleme Alındı"

    val finalize = "Sonuçlandır"

    val save = "Kaydet"

    val activityStarted = "Aktiviteye Başlandı"

    val jobReturn = "İş İade Edildi"

    val tempKindWorkOrder = 2

    val tempKindRequest = 1

    val workOrderUtilList = listOf("İş Durumlarını Filtere",assignedToPerson,waitingForApproval,
        completed,deniedWork)

    fun getRequestStatusColor(requestCase: String): Int {
        val color = when (requestCase) {
            assignedToPerson -> Color.BLUE
            waitingForApproval -> Color.MAGENTA
            completed -> Color.parseColor("#006400")
            deniedWork -> Color.RED
            else -> Color.BLACK
        }

        return color
    }

}