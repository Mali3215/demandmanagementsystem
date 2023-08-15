package com.example.demandmanagementsystem.view


import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.databinding.ActivityMyWorkOrderDetailBinding
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.util.WorkOrderUtil
import com.example.demandmanagementsystem.viewmodel.MyWorkOrderDetailViewModel

class MyWorkOrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyWorkOrderDetailBinding
    private lateinit var viewModel: MyWorkOrderDetailViewModel
    private var myWorkOrderID: String? = null
    private val util = WorkOrderUtil()
    private val utilRequest = RequestUtil()
    private lateinit var spinnerDataAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@MyWorkOrderDetailActivity
            , R.layout.activity_my_work_order_detail)

        binding.toolbarWorkOrder.title = "MyWorkOrder"
        setSupportActionBar(binding.toolbarWorkOrder)

        viewModel = ViewModelProvider(this).get(MyWorkOrderDetailViewModel::class.java)

        myWorkOrderID = intent.getStringExtra(util.intentWorkOrderId)
        viewModel.getData(myWorkOrderID!!)

        viewModel.workOrderData.observe(this) {workOrder ->

            if (workOrder != null) {
                if ((workOrder.workOrderDescription.toString() == "") && (workOrder.workOrderSubject.toString() == "")){

                    binding.relativeWorkOrderInfo.visibility = View.GONE
                } else {

                    binding.relativeWorkOrderInfo.visibility = View.VISIBLE
                }


                if ((workOrder.workOrderRequestDescription.toString() == "") && (workOrder.workOrderRequestSubject.toString() == "")){

                    binding.relativeLayoutRequestDetail.visibility = View.GONE
                } else {

                    binding.relativeLayoutRequestDetail.visibility = View.VISIBLE
                }

            }

        }

        viewModel.workOrderData.observe(this) { workOrderData ->
            binding.selectedWorkOrder = workOrderData
            if (workOrderData?.workOrderCase == util.assignedToPerson) {
                viewModel.getAuthorityType {currentUserId ->
                    if (workOrderData.selectedWorkOrderUserId == currentUserId){
                        binding.toolbarWorkOrder.menu.findItem(R.id.denied).isVisible = true
                        binding.toolbarWorkOrder.menu.findItem(R.id.jobReturn).isVisible = true
                        binding.toolbarWorkOrder.menu.findItem(R.id.createWorkOrderDetailMenu).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.startedActivity).isVisible = true
                        binding.toolbarWorkOrder.menu.findItem(R.id.denied).isVisible = false
                    } else {
                        binding.toolbarWorkOrder.menu.findItem(R.id.completed).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.denied).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.jobReturn).isVisible = false
                         binding.toolbarWorkOrder.menu.findItem(R.id.save).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.startedActivity).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.createWorkOrderDetailMenu).isVisible = false

                    }

                }

            } else if (workOrderData?.workOrderCase == util.waitingForApproval) {

                viewModel.getAuthorityType {currentUserId ->
                    if (workOrderData.createWorkOrderId == currentUserId){

                        binding.toolbarWorkOrder.menu.findItem(R.id.confirmJob).isVisible = true
                        binding.spinnerWorkOrderUserSubject.visibility = View.GONE
                        binding.relativeLayoutWorkOrderDetail.visibility = View.VISIBLE
                        binding.toolbarWorkOrder.menu.findItem(R.id.completed).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.denied).isVisible = true
                        binding.toolbarWorkOrder.menu.findItem(R.id.createWorkOrderDetailMenu).isVisible = false
                    } else {
                        binding.toolbarWorkOrder.menu.findItem(R.id.completed).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.denied).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.jobReturn).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.save).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.startedActivity).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.createWorkOrderDetailMenu).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.confirmJob).isVisible = false
                        binding.spinnerWorkOrderUserSubject.visibility = View.GONE
                        binding.relativeLayoutWorkOrderDetail.visibility = View.VISIBLE

                    }

                }
            }else if (workOrderData?.workOrderCase == util.completed) {

                binding.spinnerWorkOrderUserSubject.visibility = View.GONE
                binding.relativeLayoutWorkOrderDetail.visibility = View.VISIBLE
                binding.toolbarWorkOrder.menu.findItem(R.id.completed).isVisible = false
                binding.toolbarWorkOrder.menu.findItem(R.id.denied).isVisible = false
                binding.toolbarWorkOrder.menu.findItem(R.id.jobReturn).isVisible = false
                binding.toolbarWorkOrder.menu.findItem(R.id.save).isVisible = false
                binding.toolbarWorkOrder.menu.findItem(R.id.startedActivity).isVisible = false
                binding.toolbarWorkOrder.menu.findItem(R.id.createWorkOrderDetailMenu).isVisible = false
                binding.toolbarWorkOrder.menu.findItem(R.id.confirmJob).isVisible = false

            }else if (workOrderData?.workOrderCase == util.activityProcessed) {

                viewModel.getAuthorityType {currentUserId ->
                    if (workOrderData.selectedWorkOrderUserId == currentUserId){

                        binding.relativeLayoutWorkOrderDetail.visibility = View.VISIBLE

                        viewModel.getDataSpinnerRequest { list ->
                            Log.e("myactivity", "=> ${list.size}")
                            getSpinnerRequestData(list)

                            binding.toolbarWorkOrder.menu.findItem(R.id.save).isVisible = true
                            binding.toolbarWorkOrder.menu.findItem(R.id.completed).isVisible = true
                            binding.toolbarWorkOrder.menu.findItem(R.id.createWorkOrderDetailMenu).isVisible = false


                        }



                    } else {

                        binding.textWorkOrderUserSubject.apply {
                            setTextColor(ContextCompat.getColor(this@MyWorkOrderDetailActivity, R.color.black))
                            inputType = InputType.TYPE_NULL
                            isEnabled = false
                        }
                        binding.toolbarWorkOrder.menu.findItem(R.id.createWorkOrderDetailMenu).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.completed).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.denied).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.jobReturn).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.save).isVisible = false
                        binding.toolbarWorkOrder.menu.findItem(R.id.startedActivity).isVisible = false

                    }

                }

            }else if (workOrderData?.workOrderCase == util.deniedWork) {
                binding.toolbarWorkOrder.menu.findItem(R.id.createWorkOrderDetailMenu).isVisible = false
                binding.relativeLayoutWorkOrderDetail.visibility = View.VISIBLE
                binding.spinnerWorkOrderUserSubject.visibility = View.GONE


            }else if (workOrderData?.workOrderCase == util.jobReturn) {
                binding.toolbarWorkOrder.menu.findItem(R.id.createWorkOrderDetailMenu).isVisible = false
                viewModel.getAuthorityType {currentUserId ->
                    if (workOrderData.createWorkOrderId == currentUserId){
                        binding.toolbarWorkOrder.menu.findItem(R.id.createWorkOrderDetailMenu).isVisible = true
                        binding.toolbarWorkOrder.menu.findItem(R.id.denied).isVisible = true
                        binding.relativeLayoutWorkOrderDetail.visibility = View.GONE
                        binding.spinnerWorkOrderUserSubject.visibility = View.GONE
                    }
                }
            }
        }
        binding.openMenuButtonWorkOrderResult.setOnClickListener {
            if (binding.menuCardViewWorkOrderResult.visibility == View.VISIBLE) {
                binding.menuCardViewWorkOrderResult.visibility = View.GONE

            } else {
                binding.menuCardViewWorkOrderResult.visibility = View.VISIBLE
            }
        }



        binding.openMenuButtonWorkOrder.setOnClickListener {

            if (binding.menuCardViewWorkOrder.visibility == View.VISIBLE) {
                binding.menuCardViewWorkOrder.visibility = View.GONE

            } else {
                binding.menuCardViewWorkOrder.visibility = View.VISIBLE
            }
        }

        binding.openMenuButtonWorkOrderRequest.setOnClickListener {
            if (binding.menuCardViewWorkOrderRequest.visibility == View.VISIBLE) {
                binding.menuCardViewWorkOrderRequest.visibility = View.GONE

            } else {
                binding.menuCardViewWorkOrderRequest.visibility = View.VISIBLE
            }
        }


    }
    fun getSpinnerRequestData(spinnerList: List<String>){

        spinnerDataAdapter = ArrayAdapter(this@MyWorkOrderDetailActivity, android.R.layout.simple_list_item_1,
            android.R.id.text1, spinnerList
        )

        binding.spinnerWorkOrderUserSubject.adapter = spinnerDataAdapter

        val initialSelection = spinnerList.indexOf(viewModel.workOrderUserSubject.value)
        if (initialSelection != -1) {
            binding.spinnerWorkOrderUserSubject.setSelection(initialSelection)
        }

        binding.spinnerWorkOrderUserSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, indeks: Int, p3: Long) {

                binding.textWorkOrderUserSubject.setText(spinnerList[indeks])

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.completed -> {

                viewModel.workOrderData.observe(this) {workOrder ->

                    if (workOrder != null) {
                        if ((workOrder.workOrderDescription.toString() == "") && (workOrder.workOrderSubject.toString() == "")){

                            viewModel.menuComletedUpdate(this@MyWorkOrderDetailActivity
                                , util.tempKindRequest,binding)
                        } else {

                            viewModel.menuComletedUpdate(this@MyWorkOrderDetailActivity
                                , util.tempKindWorkOrder,binding)
                        }

                    }

                }

                true
            }
            R.id.denied -> {

                if (binding.textWorkOrderRequestSubject.text.toString() == ""){
                    viewModel.menuDeniedUpdate(this@MyWorkOrderDetailActivity,binding
                        ,util.tempKindWorkOrder)
                }else {
                    viewModel.menuDeniedUpdate(this@MyWorkOrderDetailActivity,binding
                        ,util.tempKindRequest)
                }

                true
            }
            R.id.save ->{
                myWorkOrderID?.let {
                    viewModel.workOrderSave(binding,this, it)
                }

                true
            }
            R.id.startedActivity ->{

                binding.relativeLayoutWorkOrderDetail.visibility = View.VISIBLE

                myWorkOrderID?.let {
                    viewModel.startedActivity(this,binding, it)
                }

                true
            }

            R.id.jobReturn -> {

                myWorkOrderID?.let {
                    viewModel.jobReturn(binding,this, it)
                }

                true
            }

            R.id.createWorkOrderDetailMenu -> {
                viewModel.workOrderData.observe(this) { workOrderData ->
                    if (workOrderData!!.workOrderRequestId == ""){
                        val intent = Intent(this@MyWorkOrderDetailActivity,CreateWorkOrderActivity::class.java)
                        intent.putExtra(util.intentWorkOrderId,workOrderData?.workOrderID)
                        intent.putExtra(utilRequest.intentRequestId,workOrderData?.workOrderRequestId)
                        intent.putExtra(utilRequest.intentRequestDetail,2)
                        startActivity(intent)
                    }else{
                        val intent = Intent(this@MyWorkOrderDetailActivity,CreateWorkOrderActivity::class.java)
                        intent.putExtra(util.intentWorkOrderId,workOrderData?.workOrderID)
                        intent.putExtra(utilRequest.intentRequestId,workOrderData?.workOrderRequestId)
                        intent.putExtra(utilRequest.intentRequestDetail,1)
                        startActivity(intent)
                    }


                }
                true
            }
            R.id.confirmJob -> {
                viewModel.workOrderData.observe(this) { workOrderData ->
                    if (workOrderData != null) {

                        viewModel.workCompleted(myWorkOrderID!!,this@MyWorkOrderDetailActivity,
                            workOrderData.workOrderRequestId!!)
                    }
                    val intent = Intent(this@MyWorkOrderDetailActivity, CreatedWorkOrderActivity::class.java)
                    finish()
                    startActivity(intent)
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.work_order_detail_menu, menu)
        return true
    }


}