package com.example.demandmanagementsystem.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.databinding.ActivityAddPersonBinding
import com.example.demandmanagementsystem.databinding.ActivityUserProfileInfoBinding
import com.example.demandmanagementsystem.databinding.ContentProfileBinding
import com.example.demandmanagementsystem.viewmodel.RequestDetailViewModel
import com.example.demandmanagementsystem.viewmodel.UserProfileInfoViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UserProfileInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileInfoBinding
    private lateinit var bindingContentProfileBinding: ContentProfileBinding
    private lateinit var viewModel: UserProfileInfoViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@UserProfileInfoActivity,R.layout.activity_user_profile_info)
        bindingContentProfileBinding = binding.contentProfile
        viewModel = ViewModelProvider(this).get(UserProfileInfoViewModel::class.java)

        viewModel.getData {
            binding.userData = it
            bindingContentProfileBinding.userData = it
        }

        bindingContentProfileBinding.saveButton.setOnClickListener {
            showChangePasswordPopup()
        }



    }

    private fun showChangePasswordPopup() {
        val popupView = layoutInflater.inflate(R.layout.popup_change_password, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(popupView)
            .show()

        val newPasswordEditText: EditText = popupView.findViewById(R.id.editTextNewPassword)
        val confirmPasswordEditText: EditText = popupView.findViewById(R.id.editTextConfirmPassword)
        val confirmButton: Button = popupView.findViewById(R.id.buttonChangePassword)

        confirmButton.setOnClickListener {
            // Yeni parola işlemlerini burada gerçekleştir
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (newPassword == confirmPassword) {
                viewModel.resetPassword(newPassword,this@UserProfileInfoActivity)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Şifreler Eşleşmiyor. Tekrar Deneyiniz.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}