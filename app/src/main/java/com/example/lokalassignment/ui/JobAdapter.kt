package com.example.lokalassignment.ui


import com.example.lokalassignment.model.Job
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lokalassignment.databinding.CardJobBinding
import com.example.lokalassignment.util.Utils

class JobAdapter(private val onItemClick:(Job)->Unit ):ListAdapter<Job,JobAdapter.JobViewHolder>(JobDiffCallback()){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class JobViewHolder(private val binding: CardJobBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(job: Job) {
            binding.apply {
                // Set the job title or fallback to "N/A"
                tvJobTitle.text = job.title ?: "N/A"

                // Set the salary or fallback to "Not specified"
                tvSalary.text = job.amount ?: "Not specified"

                // Set the company name or fallback to "Unknown Company"
                tvCompany.text = job.company_name ?: "Unknown Company"

                // Set the location slug or fallback to "No location provided"
                tvLocation.text = job.job_location_slug ?: "No location provided"

                // Set the button text or default it to "Contact HR"
                btnCallHR.text = job.button_text ?: "Contact HR"

                // Set the fee text or default to "No fees"
                tvFeeCharged.text = job.fees_text ?: "No fees"

                // Handle job tags (now a list of `JobTag` objects)
                val jobTags = job.job_tags
                if (jobTags != null && jobTags.isNotEmpty()) {
                    // Assuming you're showing the first tag in the list for simplicity
                    val firstTag = jobTags[0]
                    tvVacancies.text = firstTag.value ?: "No tags"

                    // Optionally, if you want to show tag colors:
                    tvVacancies.setBackgroundColor(Color.parseColor(firstTag.bg_color ?: "#FFFFFF"))
                    tvVacancies.setTextColor(Color.parseColor(firstTag.text_color ?: "#000000"))
                } else {
                    tvVacancies.text = "No tags available"
                }

                // Handle the chat button for WhatsApp contact preferences
                btnChat.setOnClickListener {
                    val contactPreference = job.contact_preference
                    if (contactPreference?.preference == 1) {
                        val whatsappLink = contactPreference.whatsapp_link
                        if (!whatsappLink.isNullOrEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(whatsappLink)
                            try {
                                itemView.context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Utils.showToast("WhatsApp is not installed on this device.", itemView.context)
                            }
                        }
                    }
                }

                // Set a click listener for the root view to handle job item clicks
                root.setOnClickListener { onItemClick(job) }
            }
        }
    }
}
class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}