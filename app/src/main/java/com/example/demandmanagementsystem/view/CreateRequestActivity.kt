package com.example.demandmanagementsystem.view

import android.content.Context
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
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.databinding.ActivityCreateRequestBinding
import com.example.demandmanagementsystem.model.CreateRequest
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.viewmodel.CreateRequestViewModel

class CreateRequestActivity : AppCompatActivity() , AlertDialogListener {
    private val reference= FirebaseServiceReference()
    private lateinit var binding: ActivityCreateRequestBinding
    private lateinit var viewModel: CreateRequestViewModel
    private val departmentTypeList = ArrayList<String>()
    private lateinit var spinnerDataAdapter: ArrayAdapter<String>
    private lateinit var spinnerDataJobTypeAdapter: ArrayAdapter<String>
    private lateinit var spinnerDataSubJobTypeAdapter: ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@CreateRequestActivity
            , R.layout.activity_create_request)

        binding.toolbarRequest.title = "Request"
        setSupportActionBar(binding.toolbarRequest)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel = ViewModelProvider(this).get(CreateRequestViewModel::class.java)

        viewModel.requestFill(binding)

        viewModel.departmentTypeList.observe(this, Observer { departmentList ->
            departmentList?.let {
                departmentTypeList.clear()
                departmentTypeList.addAll(it)
                spinnerDataAdapter.notifyDataSetChanged()
            }
        })

        viewModel.getJobDetails()

        spinnerDataAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1
            , android.R.id.text1, departmentTypeList)
        binding.spinnerRequestDepartment.adapter = spinnerDataAdapter

        binding.spinnerRequestDepartment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, indeks: Int, p3: Long) {
                binding.textRequestType.setText("")
                binding.textRequestSubject.setText("")
                binding.textRequestSendDepartment.setText(departmentTypeList[indeks])


                spinnerDataJobTypeAdapter = ArrayAdapter(this@CreateRequestActivity
                    ,android.R.layout.simple_list_item_1, android.R.id.text1,listOf())

                binding.spinnerRequestType.adapter = spinnerDataJobTypeAdapter

                spinnerDataSubJobTypeAdapter = ArrayAdapter(this@CreateRequestActivity,
                    android.R.layout.simple_list_item_1,android.R.id.text1, listOf()
                )
                binding.spinnerRequestSubject.adapter = spinnerDataSubJobTypeAdapter

                spinner(departmentTypeList[indeks])

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

    }

    fun spinner(temp: String){

        viewModel.spinnerDataJobType(temp)
        val jobTypeList = viewModel.getJobDetailsJobTypeList() ?: listOf()
        spinnerDataJobTypeAdapter = ArrayAdapter(this@CreateRequestActivity,android.R.layout.simple_list_item_1, android.R.id.text1,jobTypeList)

        binding.spinnerRequestType.adapter = spinnerDataJobTypeAdapter

        binding.spinnerRequestType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                binding.textRequestSubject.setText("")
                binding.textRequestType.setText(jobTypeList[p2])



                spinnerSubtype(jobTypeList[p2])

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }

    fun spinnerSubtype(temp: String){

        viewModel.spinnerDataSubType(temp)

        val subTypeList = viewModel.getJobDetailsSubTypeList() ?: listOf()
        spinnerDataSubJobTypeAdapter = ArrayAdapter(this@CreateRequestActivity,
            android.R.layout.simple_list_item_1,android.R.id.text1,subTypeList)
        binding.spinnerRequestSubject.adapter = spinnerDataSubJobTypeAdapter

        binding.spinnerRequestSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                binding.textRequestSubject.setText("")
                binding.textRequestSubject.setText(subTypeList[p2])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.request_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reject -> {
                spinnerDataJobTypeAdapter = ArrayAdapter(this@CreateRequestActivity,android.R.layout.simple_list_item_1, android.R.id.text1,listOf())

                binding.spinnerRequestType.adapter = spinnerDataJobTypeAdapter

                spinnerDataSubJobTypeAdapter = ArrayAdapter(this@CreateRequestActivity,
                    android.R.layout.simple_list_item_1,android.R.id.text1, listOf()
                )
                binding.spinnerRequestSubject.adapter = spinnerDataSubJobTypeAdapter
                binding.textRequestSubject.setText("")
                binding.textRequestDescription.setText("")
                binding.textRequestType.setText("")

                true
            }
            R.id.workOrderCreate -> {

                val alerDialog = AlertDialog.Builder(this@CreateRequestActivity)

                alerDialog.setMessage("Talep Oluşturulsun Mu?")
                alerDialog.setPositiveButton("Evet"){ dialogInterface, i ->


                    val requestType = binding.textRequestType.text.toString()
                    val requestName = binding.textRequestUserName.text.toString()
                    val requestDepartment = binding.textRequestDepartment.text.toString()
                    val requestSendDepartment = binding.textRequestSendDepartment.text.toString()
                    val requestSubject = binding.textRequestSubject.text.toString()
                    val requestDescription = binding.textRequestDescription.text.toString()
                    val requestContactNumber = binding.textRequestContactNumber.text.toString()

                    viewModel.createRequest(
                        CreateRequest(
                            requestType,
                            requestName,
                            requestDepartment,
                            requestSendDepartment,
                            requestSubject,
                            requestDescription,
                            requestContactNumber
                        ),this@CreateRequestActivity
                    )

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

    override fun showAlertDialog() {
        val sharedPreferences = getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
        reference.sigInOut(sharedPreferences, this@CreateRequestActivity)
    }



}