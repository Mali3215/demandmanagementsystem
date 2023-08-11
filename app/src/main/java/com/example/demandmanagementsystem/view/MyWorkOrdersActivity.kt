package com.example.demandmanagementsystem.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.adapter.MyWorkOrdersAdapter
import com.example.demandmanagementsystem.adapter.RequestAdapter
import com.example.demandmanagementsystem.databinding.ActivityMyWorkOrdersBinding
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.util.WorkOrderUtil
import com.example.demandmanagementsystem.viewmodel.CreateWorkOrderViewModel
import com.example.demandmanagementsystem.viewmodel.DemandListViewModel
import com.example.demandmanagementsystem.viewmodel.MyWorkOrdersViewModel

class MyWorkOrdersActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var binding: ActivityMyWorkOrdersBinding
    private lateinit var viewModel: MyWorkOrdersViewModel
    private lateinit var adapter: MyWorkOrdersAdapter
    private val util = WorkOrderUtil()
    private lateinit  var spinnerDataAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@MyWorkOrdersActivity
            , R.layout.activity_my_work_orders)

        viewModel = ViewModelProvider(this).get(MyWorkOrdersViewModel::class.java)


        binding.recyclerViewMyWorkOrderList.setHasFixedSize(true)
        binding.recyclerViewMyWorkOrderList.layoutManager = LinearLayoutManager(this@MyWorkOrdersActivity)

        binding.toolbarMyWorkOrder.title = "MyWorkOrder"
        binding.toolbarMyWorkOrder.visibility = View.VISIBLE
        setSupportActionBar(binding.toolbarMyWorkOrder)


        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchData()
            viewModel.getData()

            binding.swipeRefreshLayout.isRefreshing = false
        }
        viewModel.myWorkOrderLoading.observe(this) { loading ->
            if (loading) {
                binding.myWorkOrderListloading.visibility = View.VISIBLE
                binding.recyclerViewMyWorkOrderList.visibility = View.GONE
            } else {
                binding.myWorkOrderListloading.visibility = View.GONE
                binding.recyclerViewMyWorkOrderList.visibility = View.VISIBLE
            }
        }

        viewModel.getMyOrderWorkList().observe(this@MyWorkOrdersActivity) { workOrder ->

            adapter = MyWorkOrdersAdapter(this@MyWorkOrdersActivity, workOrder!!)
            binding.recyclerViewMyWorkOrderList.adapter = adapter
        }
        
        viewModel.getData()
        viewModel.fetchData()

        spinnerDataAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,
            android.R.id.text1,util.workOrderUtilList)

        binding.spinnerWorkOrderFilter.adapter = spinnerDataAdapter

        binding.spinnerWorkOrderFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedFilter = util.workOrderUtilList[p2]

                if(p2 == 0){
                    viewModel.getMyOrderWorkList().observe(this@MyWorkOrdersActivity) { workOrder ->

                        adapter = MyWorkOrdersAdapter(this@MyWorkOrdersActivity, workOrder!!)
                        binding.recyclerViewMyWorkOrderList.adapter = adapter
                    }

                    viewModel.getData()
                    viewModel.fetchData()
                }else{
                    viewModel.filterList(selectedFilter)

                    viewModel.getWorkOrderFilterList().observe(this@MyWorkOrdersActivity) { workOrder ->

                        adapter = MyWorkOrdersAdapter(applicationContext, workOrder!!)
                        binding.recyclerViewMyWorkOrderList.adapter = adapter
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }


    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu,menu)

        val item = menu.findItem(R.id.search_demand)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(this@MyWorkOrdersActivity)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                viewModel.fetchData()
                viewModel.getData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            Log.e("onQueryTextChange",newText)
        }
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            Log.e("onQueryTextSubmit",query)
        }
        return true
    }


}