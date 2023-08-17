package com.example.demandmanagementsystem.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.demandmanagementsystem.databinding.ActivityMainBinding
import com.example.demandmanagementsystem.util.GetUserSaveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth;
    private val getUserData = GetUserSaveData()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        binding.buttonEnter.setOnClickListener {

            val email=binding.textUserName.text.toString()
            val password=binding.textPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val user = auth.currentUser
                        val userId = user!!.uid
                        lifecycleScope.launch {
                            val success = getUserData.getUserSaveData(userId, this@MainActivity)

                            if (success) {
                                Toast.makeText(this@MainActivity, "Hoşgeldiniz!", Toast.LENGTH_LONG)
                                    .show()

                                val sp = getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
                                Log.e(
                                    "MainActivity",
                                    "userId " + sp.getString("userId", "Boş")
                                )
                                Log.e(
                                    "MainActivity",
                                    "tcIdentityNo " + sp.getString("tcIdentityNo", "Boş")
                                )
                                Log.e("MainActivity", "email " + sp.getString("email", "Boş"))
                                Log.e("MainActivity", "name " + sp.getString("name", "Boş"))
                                Log.e("MainActivity", "password " + sp.getString("password", "Boş"))
                                Log.e("MainActivity", "telNo " + sp.getString("telNo", "Boş"))
                                Log.e(
                                    "MainActivity",
                                    "authorityType " + sp.getString("authorityType", "Boş")
                                )
                                Log.e(
                                    "MainActivity",
                                    "departmentType " + sp.getString("departmentType", "Boş")
                                )

                                val intent =
                                    Intent(this@MainActivity, DemandListActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Log.e("MainActivity","oncreate => lifecycle da hata")
                            }

                        }
                    }

                }.addOnFailureListener { exception ->

                    Log.e("MainActivity","oncreate => ${exception.localizedMessage}")
                    Toast.makeText(this@MainActivity, "Kullanıcı Adı veya Şifre Hatalı", Toast.LENGTH_LONG).show()
                    
                }
            }else{
                Toast.makeText(this@MainActivity,"Bilgileriniz Boş Olamaz",Toast.LENGTH_LONG).show()
            }

        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        if (currentUser != null){
            val sp = getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
            Log.e("MainActivity","id "+sp.getString("userId", "Boş"))
            Log.e("MainActivity","tcIdentityNo "+sp.getString("tcIdentityNo", "Boş"))
            Log.e("MainActivity","email "+sp.getString("email", "Boş"))
            Log.e("MainActivity","name "+sp.getString("name", "Boş"))
            Log.e("MainActivity","password "+sp.getString("password", "Boş"))
            Log.e("MainActivity","telNo "+sp.getString("telNo", "Boş"))
            Log.e("MainActivity","authorityType "+sp.getString("authorityType", "Boş"))
            Log.e("MainActivity","departmentType "+sp.getString("departmentType", "Boş"))
            Toast.makeText(this@MainActivity, "Hoşgeldiniz!", Toast.LENGTH_LONG).show()
            val intent = Intent(this@MainActivity, DemandListActivity::class.java)
            startActivity(intent)
            finish()
        }


    }




}