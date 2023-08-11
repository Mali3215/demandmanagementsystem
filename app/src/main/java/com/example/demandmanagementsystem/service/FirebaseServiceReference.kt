package com.example.demandmanagementsystem.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseServiceReference {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getFirebaseAuth(): FirebaseAuth {
        return auth
    }

    fun usersCollection(): CollectionReference {
        return firestore.collection("users")
    }

    fun requestsCollection(): CollectionReference {
        return firestore.collection("requests")
    }

    fun workordersCollection(): CollectionReference {
        return firestore.collection("workorders")
    }

    fun departmentTypeCollection(): CollectionReference {
        return firestore.collection("departmentType")
    }

    fun employeeCollection(): CollectionReference {
        return firestore.collection("employee")
    }

    fun jobDetailsCollection(): CollectionReference {
        return firestore.collection("jobdetails")
    }

    fun statusCollection(): CollectionReference {
        return firestore.collection("status")
    }


}