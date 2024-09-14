package com.example.lokalassignment.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lokalassignment.api.RetrofitInstance
import com.example.lokalassignment.databinding.FragmentJobBinding
import com.example.lokalassignment.repository.JobRepository
import com.example.lokalassignment.ui.viewmodels.JobViewModel
import com.example.lokalassignment.ui.viewmodels.JobViewModelProviderFactory
import com.example.lokalassignment.util.Resource
import com.example.lokalassignment.util.Utils


class JobFragment : Fragment() {

    private var _binding: FragmentJobBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: JobViewModel
    private lateinit var jobAdapter: JobAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentJobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the jobApi and ViewModel
        val jobApi = RetrofitInstance.api
        val factory = JobViewModelProviderFactory(requireActivity().application, JobRepository(jobApi))
        viewModel = ViewModelProvider(this, factory).get(JobViewModel::class.java)

        // Initialize the adapter
        jobAdapter = JobAdapter { job ->
//             Handle job item click if needed
            context?.let { Utils.showToast("Clicked: ${job.title}", it) }
        }

        setupRecyclerView()
        observeJobs()
    }

    private fun setupRecyclerView() {
        // Setup RecyclerView with the initialized adapter
        binding.rvJobList.apply {
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeJobs() {
        viewModel.JobList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    response.data?.let { jobResponse ->

                        println("Jobs received: ${jobResponse.jobs?.size ?: 0}")
                        if (jobResponse.jobs.isNullOrEmpty()) {
                            showEmptyState("No jobs available")
                        } else {
                            jobAdapter.submitList(jobResponse.jobs)
                            binding.rvJobList.visibility = View.VISIBLE
                        }
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    response.message?.let { message ->
                        println("Error: $message")
                        println("$response")
                        showEmptyState(message)
                    }
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    println("Loading jobs...")
                }
            }
        }
    }

    private fun showEmptyState(message: String) {
        binding.rvJobList.visibility = View.GONE
        binding.tvEmptyState.apply {
            text = message
            visibility = View.VISIBLE
        }
        context?.let { Utils.showToast(message, it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
