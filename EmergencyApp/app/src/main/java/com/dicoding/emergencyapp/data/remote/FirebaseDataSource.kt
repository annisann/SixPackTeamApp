package com.dicoding.emergencyapp.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dicoding.emergencyapp.data.entity.ReportEntity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseDataSource {

    private val database = Firebase.database
    private val myRef = database.getReference("users")

    private var _uploadStatus = MutableLiveData<Boolean>()
    var uploadStatus: LiveData<Boolean> = _uploadStatus

    fun uploadData(
        timestamp: String,
        userId: String?,
        transcription: String,
        report: String,
        latitude: Double?,
        longitude: Double?,
        status: String
    ){
        val userRef = myRef.child(userId.toString())
        val reportRef = userRef.push().key.toString()
        val report = ReportEntity(
            timestamp,
            transcription,
            report,
            latitude,
            longitude,
            status
        )
        userRef.child(reportRef).setValue(report).addOnCompleteListener {
            _uploadStatus.value = it.isSuccessful
        }
    }

}