package com.example.demandmanagementsystem.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.demandmanagementsystem.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.buttonEnter.setOnClickListener {

            val email=binding.textUserName.text.toString()
            val password=binding.textPassword.text.toString()

            if(email.isNotEmpty() || password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(this@MainActivity, "Hoşgeldiniz!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@MainActivity, DemandListActivity::class.java)
                        startActivity(intent)
                        finish()

                    }

                }.addOnFailureListener { exception ->
                    Log.e("Hata","${exception.localizedMessage}")
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
            Toast.makeText(this@MainActivity, "Hoşgeldiniz!", Toast.LENGTH_LONG).show()
            val intent = Intent(this@MainActivity, DemandListActivity::class.java)
            startActivity(intent)
            finish()
        }


    }




}