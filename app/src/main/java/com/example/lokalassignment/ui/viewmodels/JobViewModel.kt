package com.example.lokalassignment.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lokalassignment.JobApplication
import com.example.lokalassignment.model.JobResponse
import com.example.lokalassignment.repository.JobRepository
import com.example.lokalassignment.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class JobViewModel(
    app: Application,
    private val jobRepository : JobRepository
    ):AndroidViewModel(app) {
    val JobList : MutableLiveData<Resource<JobResponse>> = MutableLiveData()
    private var JobListPage = 1
    private var JobListResponse: JobResponse ?=null


    init {
        getJobs()
    }

    private fun getJobs() = viewModelScope.launch{
        safeGetJobs()
    }

    private fun handleJobResponse(response: Response<JobResponse>): Resource<JobResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (resultResponse.results == null) {
                    return Resource.Error("No jobs available")
                }
                if (JobListResponse == null) {
                    JobListResponse = resultResponse
                }
                return Resource.Success(JobListResponse ?: resultResponse)
            } ?: return Resource.Error("Empty response body")
        }
        return Resource.Error(response.message())
    }

    private suspend fun safeGetJobs(){
        JobList.postValue(Resource.Loading())
        try{
            if(hasInternetConnection()){
                val response = jobRepository.getJobs(JobListPage)
                JobList.postValue(handleJobResponse(response))
            }else
                JobList.postValue(Resource.Error("No Internet"))
        }catch(t:Throwable){
            when(t){
                is IOException -> JobList.postValue(Resource.Error("Network Failure"))
                else ->JobList.postValue(Resource.Error("Conversion Error"))
            }
        }
    }



    private fun hasInternetConnection() : Boolean{
        val connectivityManager = getApplication<JobApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

            return when{
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)-> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)-> true
                else -> false
            }
        }
        return false
    }
}