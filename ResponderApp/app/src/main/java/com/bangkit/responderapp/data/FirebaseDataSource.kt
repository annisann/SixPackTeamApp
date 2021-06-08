package com.bangkit.responderapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseDataSource {

    private val database = Firebase.database
    private val myRef = database.getReference("users")

    private var _readSuccess = MutableLiveData<Boolean>()
    var readSuccess: LiveData<Boolean> = _readSuccess

    private var _isLoading = MutableLiveData<Boolean>()
    var isLoading: LiveData<Boolean> = _isLoading

    fun getAllReports(): MutableLiveData<ArrayList<ReportEntity?>>{
        _readSuccess.value = true
        _isLoading.value = true
        val result = MutableLiveData<ArrayList<ReportEntity?>>()
        val reportList = arrayListOf<ReportEntity?>()
        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (user in snapshot.children){
                        for (report in user.children){
                            val reportObject = report.getValue(ReportEntity::class.java)
                            reportList.add(reportObject)
                        }
                    }
                    reportList.reverse()
                    result.value = reportList
                    _isLoading.value = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _readSuccess.value = false
                _isLoading.value = false
            }

        })
        return result
    }


}