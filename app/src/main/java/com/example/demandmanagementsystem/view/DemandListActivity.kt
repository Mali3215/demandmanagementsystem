package com.example.demandmanagementsystem.view


import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.adapter.RequestAdapter
import com.example.demandmanagementsystem.databinding.ActivityDemandListBinding
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.viewmodel.DemandListViewModel

class DemandListActivity : AppCompatActivity()
    ,SearchView.OnQueryTextListener{

    private lateinit var viewModel: DemandListViewModel
    private lateinit var binding: ActivityDemandListBinding
    private lateinit var adapter:RequestAdapter
    private val util = RequestUtil()
    private lateinit  var spinnerDataAdapter: ArrayAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDemandListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sp = getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)

        Log.e("DemandListActivitys","tcIdentityNo "+sp.getString("tcIdentityNo", "Boş"))
        Log.e("DemandListActivitys","email "+sp.getString("email", "Boş"))
        Log.e("DemandListActivitys","name "+sp.getString("name", "Boş"))
        Log.e("DemandListActivitys","password "+sp.getString("password", "Boş"))
        Log.e("DemandListActivitys","telNo "+sp.getString("telNo", "Boş"))
        Log.e("DemandListActivitys","authorityType "+sp.getString("authorityType", "Boş"))
        Log.e("DemandListActivitys","departmentType "+sp.getString("departmentType", "Boş"))


        binding.recyclerViewDemandList.setHasFixedSize(true)
        binding.recyclerViewDemandList.layoutManager = LinearLayoutManager(this@DemandListActivity)

        binding.toolbar.title = "Başlık"
        binding.toolbar.visibility = View.VISIBLE
        setSupportActionBar(binding.toolbar)

        val baslik = binding.navigationView.inflateHeaderView(R.layout.nav_view_image_text)
        val imageView = baslik.findViewById(R.id.imageProfile) as ImageView
        imageView.setImageResource(R.drawable.batman)

        val toggle = ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, 0, 0)
        binding.drawer.addDrawerListener(toggle)
        toggle.syncState()

        viewModel = ViewModelProvider(this@DemandListActivity).get(DemandListViewModel::class.java)

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            val itemId = menuItem.itemId

            when (itemId) {
                R.id.createRequest -> viewModel.onCreateRequestClick(this@DemandListActivity)
                R.id.createWorkOrder -> viewModel.onCreateWorkOrderClick(this@DemandListActivity)
                R.id.logOut -> viewModel.onLogOutClick(this@DemandListActivity)
                R.id.createdRequests -> viewModel.onCreatedRequestsClick(this@DemandListActivity)
                R.id.addPerson -> viewModel.onAddPersonClick(this@DemandListActivity)
                R.id.myWorkOrders -> viewModel.onMyWorkOrders(this@DemandListActivity)
                R.id.createdWorkOrder -> viewModel.onCreatedWorkOrdersClick(this@DemandListActivity)
            }

            binding.drawer.closeDrawer(GravityCompat.START)
            true
        }

        viewModel.username.observe(this) { username ->
            val name = baslik.findViewById(R.id.textTitleName) as TextView
            name.text = username
        }

        viewModel.departmentType.observe(this) { departmentType ->
            val department = baslik.findViewById(R.id.textViewDepartment) as TextView
            department.text = departmentType
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.spinnerFilter.setSelection(0)
            viewModel.fetchData()
            viewModel.getData()

            binding.swipeRefreshLayout.isRefreshing = false
        }
        viewModel.requestLoading.observe(this) { loading ->
            if (loading) {
                binding.requestListloading.visibility = View.VISIBLE
                binding.recyclerViewDemandList.visibility = View.GONE
            } else {

                binding.requestListloading.visibility = View.GONE
                binding.recyclerViewDemandList.visibility = View.VISIBLE
            }
        }



        viewModel.authorityType.observe(this) { authorityType ->

            if (authorityType == "Departman Çalışanı") {

                binding.navigationView.menu.findItem(R.id.createWorkOrder).isVisible = false
                binding.navigationView.menu.findItem(R.id.addPerson).isVisible = false
                binding.navigationView.menu.findItem(R.id.createdWorkOrder).isVisible = false
            } else {
                binding.navigationView.menu.findItem(R.id.createWorkOrder).isVisible = true
                binding.navigationView.menu.findItem(R.id.addPerson).isVisible = true
                binding.navigationView.menu.findItem(R.id.createdWorkOrder).isVisible = true
            }

        }

        spinnerDataAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,
            android.R.id.text1,util.requestUtilList)

        binding.spinnerFilter.adapter = spinnerDataAdapter


        binding.spinnerFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedFilter = util.requestUtilList[p2]

                if(p2 == 0){
                    viewModel.getRequestList().observe(this@DemandListActivity) { requests ->

                        adapter = RequestAdapter(applicationContext, requests!!)
                        binding.recyclerViewDemandList.adapter = adapter
                    }

                    viewModel.getData()
                    viewModel.fetchData()
                }else{
                    viewModel.filterList(selectedFilter)

                    viewModel.getRequestFilterList().observe(this@DemandListActivity) { requests ->

                        adapter = RequestAdapter(applicationContext, requests!!)
                        binding.recyclerViewDemandList.adapter = adapter
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }


    }

    override fun onBackPressed() {
        if(binding.drawer.isDrawerOpen(GravityCompat.START)){
            binding.drawer.closeDrawer((GravityCompat.START))
        }else{
            onBackPressedDispatcher.onBackPressed()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu,menu)

        val item = menu.findItem(R.id.search_demand)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(this@DemandListActivity)
        binding.toolbar.menu.findItem(R.id.add_action).isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                binding.spinnerFilter.setSelection(0)
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