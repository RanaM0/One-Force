package com.itdocx.oneforce.Dashboard.Admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.itdocx.oneforce.Models.dataofAnouncements
import com.itdocx.oneforce.R
import com.itdocx.oneforce.databinding.FragmentUpdateAnnouncementsBinding


class UpdateAnnouncements : Fragment() {
    private var _binding: FragmentUpdateAnnouncementsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateAnnouncementsBinding.inflate(inflater, container, false)

        Glide.with(this).asGif().load(R.drawable.anim_deposit_list)
            .into(binding.ivAnnouncementsAnim)



        binding.clAnnouncementsBackBtn.setOnClickListener { findNavController().popBackStack() }


        binding.btnAddAnouncement.setOnClickListener {
            val title = binding.etAdminAnnouncementTitle.text.toString()
            val anouncement = binding.etAdminAnnouncement.text.toString()
            if (title.isEmpty() || anouncement.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please Fill all required Fields",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                updateAnnouncement(title, anouncement)
            }
        }



        return binding.root

    }
    private fun updateAnnouncement(title: String, announcement: String) {
        val announcementsRef = FirebaseDatabase.getInstance().getReference("Announcements")

        // Create an instance of dataofAnnouncements with the provided title and announcement
        val announcementData = dataofAnouncements(title, announcement)

        // Push the instance to the database reference
        announcementsRef.setValue(announcementData)
            .addOnSuccessListener {
                // Announcement updated successfully
                Toast.makeText(
                    requireContext(),
                    "Announcement updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                // Failed to update announcement data
                Toast.makeText(
                    requireContext(),
                    "Failed to update announcement data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}