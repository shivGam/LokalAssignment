package com.example.lokalassignment.repository

import com.example.lokalassignment.api.JobApi
import com.example.lokalassignment.api.RetrofitInstance
import com.example.lokalassignment.model.JobResponse
import retrofit2.Response

class JobRepository(private val jobApi: JobApi) {
    suspend fun getJobs(page: Int): Response<JobResponse> {
        val response = RetrofitInstance.api.getJobs(page)
        println("${response}")
        println("API Response Code: ${response.code()}")
        println("API Response Body: ${response.body()}")
        println("API Response Error Body: ${response.errorBody()?.string()}")
        return response
    }
}