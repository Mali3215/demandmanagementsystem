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
import com.example.demandmanagementsystem.adapter.RequestAdapter
import com.example.demandmanagementsystem.databinding.ActivityCreatedRequestsBinding
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.viewmodel.CreatedRequestsViewModel

class CreatedRequestsActivity : AppCompatActivity()
    , SearchView.OnQueryTextListener{

    private lateinit var binding: ActivityCreatedRequestsBinding
    private lateinit var viewModel: CreatedRequestsViewModel
    private lateinit var adapter: RequestAdapter
    private val util = RequestUtil()
    private lateinit  var spinnerDataAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this@CreatedRequestsActivity
            , R.layout.activity_created_requests)


        binding.recyclerViewCreatedRequests.setHasFixedSize(true)
        binding.recyclerViewCreatedRequests.layoutManager = LinearLayoutManager(this@CreatedRequestsActivity)

        binding.toolbarCreatedRequests.title = "Requests Created"
        binding.toolbarCreatedRequests.visibility = View.VISIBLE
        setSupportActionBar(binding.toolbarCreatedRequests)


        viewModel = ViewModelProvider(this).get(CreatedRequestsViewModel::class.java)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchData()
            viewModel.getData()

            binding.swipeRefreshLayout.isRefreshing = false
        }
        viewModel.createdRequestsLoading.observe(this) { loading ->
            if (loading) {
                binding.requestListloading.visibility = View.VISIBLE
                binding.recyclerViewCreatedRequests.visibility = View.GONE
            } else {
                binding.requestListloading.visibility = View.GONE
                binding.recyclerViewCreatedRequests.visibility = View.VISIBLE
            }
        }

        viewModel.getCreatedRequestsList().observe(this@CreatedRequestsActivity) { createdRequests ->

            adapter = RequestAdapter(this@CreatedRequestsActivity, createdRequests!!)
            binding.recyclerViewCreatedRequests.adapter = adapter
        }

        viewModel.getData()
        viewModel.fetchData()

        spinnerDataAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
            android.R.id.text1,util.requestUtilList)

        binding.spinnerCreatedRequestsFilter.adapter = spinnerDataAdapter

        binding.spinnerCreatedRequestsFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedFilter = util.requestUtilList[p2]

                if(p2 == 0){
                    viewModel.getCreatedRequestsList().observe(this@CreatedRequestsActivity) { createdRequests ->

                        adapter = RequestAdapter(this@CreatedRequestsActivity, createdRequests!!)
                        binding.recyclerViewCreatedRequests.adapter = adapter
                    }

                    viewModel.getData()
                    viewModel.fetchData()
                }else{
                    viewModel.filterList(selectedFilter)

                    viewModel.getCreatedFilterList().observe(this@CreatedRequestsActivity) { createdRequests ->

                        adapter = RequestAdapter(this@CreatedRequestsActivity, createdRequests!!)
                        binding.recyclerViewCreatedRequests.adapter = adapter
                    }
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }


        }



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu,menu)

        val item = menu.findItem(R.id.search_demand)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(this@CreatedRequestsActivity)

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