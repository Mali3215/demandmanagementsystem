package com.example.demandmanagementsystem.model

import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.TextWatcher

data class UserData(
    val tcIdentityNo: String = "",
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val telNo: String = "",
    val authorityType: String = "",
    val departmentType: String = ""
) {

}