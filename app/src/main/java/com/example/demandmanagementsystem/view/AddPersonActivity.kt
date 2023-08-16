package com.example.demandmanagementsystem.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.databinding.ActivityAddPersonBinding
import com.example.demandmanagementsystem.model.UserData
import com.example.demandmanagementsystem.viewmodel.AddPersonViewModel

class AddPersonActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPersonBinding
    private lateinit var viewModel: AddPersonViewModel
    private val departmentTypeList = ArrayList<String>()
    private lateinit  var spinnerDataAdapter: ArrayAdapter<String>

    private val typeOfStaffList = ArrayList<String>()
    private lateinit  var typeOfStaffAdapter: ArrayAdapter<String>


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = DataBindingUtil.setContentView(this@AddPersonActivity
                , R.layout.activity_add_person)

            viewModel = ViewModelProvider(this)[AddPersonViewModel::class.java]

            binding.toolbarAddPerson.title = "Kullanıcı Tanımları"
            setSupportActionBar(binding.toolbarAddPerson)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            viewModel.departmentTypeList.observe(this, Observer { departmentList ->
                departmentList?.let {
                    spinnerDataAdapter.clear()
                    spinnerDataAdapter.addAll(it)
                    spinnerDataAdapter.notifyDataSetChanged()
                }
            })


            spinnerDataAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1
                ,android.R.id.text1,departmentTypeList)

            binding.spinnerAddPersonDepartment.adapter = spinnerDataAdapter

            binding.spinnerAddPersonDepartment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, indeks: Int, p3: Long) {
                    binding.textAddPersonDepartmentType.setText(departmentTypeList[indeks])
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            viewModel.typeOfStaffList.observe(this, Observer { staffList ->
                staffList?.let {
                    typeOfStaffAdapter.clear()
                    typeOfStaffAdapter.addAll(it)
                    typeOfStaffAdapter.notifyDataSetChanged()
                }
            })



            typeOfStaffAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,android.R.id.text1,typeOfStaffList)

            binding.spinnerAddPersonAuthorizotionType.adapter = typeOfStaffAdapter

            binding.spinnerAddPersonAuthorizotionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, indeks: Int, p3: Long) {
                    binding.textAddPersonAuthorizotionType.setText(typeOfStaffList[indeks])
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            viewModel.departmentTypeList
            viewModel.typeOfStaffList

        }


        override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reject -> {
                binding.textAddPersonName.setText("")
                binding.textAddPersonTelNo.setText("")
                binding.textAddPersonEmail.setText("")
                binding.textAddPersonAuthorizotionType.setText("")
                binding.textAddPersonDepartmentType.setText("")
                binding.textAddPersonTC.setText("")

                true
            }
            R.id.workOrderCreate -> {

                val alerDialog = AlertDialog.Builder(this@AddPersonActivity)

                alerDialog.setMessage("Kullanıcı Eklensin mi?")
                alerDialog.setPositiveButton("Evet"){ dialogInterface, i ->

                    val tcIdentityNo = binding.textAddPersonTC.text.toString()
                    val email = binding.textAddPersonEmail.text.toString()
                    val password = tcIdentityNo.substring(0, 6)
                    val name = binding.textAddPersonName.text.toString()
                    val telNo = binding.textAddPersonTelNo.text.toString()
                    val authorityType = binding.textAddPersonAuthorizotionType.text.toString()
                    val departmentType = binding.textAddPersonDepartmentType.text.toString()

                    val userData = UserData(tcIdentityNo, email, name,password, telNo, authorityType, departmentType)

                    viewModel.addUser(userData,this@AddPersonActivity)

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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.request_menu, menu)
        return true
    }


}