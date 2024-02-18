package com.itdocx.oneforce.Dashboard.User

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneforce.Models.dataOfcomplains
import com.itdocx.oneforce.Models.dataofUser
import com.itdocx.oneforce.R
import com.itdocx.oneforce.databinding.FragmentUserProfileBinding
import java.util.UUID


class UserProfileFrag : Fragment() {


    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)


        fetchUserProfile()

        onToggleButtons(binding.btnHelpCenter)

        binding.btnHelpCenter.setOnClickListener {
            onToggleButtons(binding.btnHelpCenter)

        }
        binding.btnProfile.setOnClickListener {
            onToggleButtons(binding.btnProfile)

        }



        binding.btnSubmitReq.setOnClickListener {
            val ucuserName = binding.etHpuserName.text.toString()
            val ucuserComplain = binding.etHpDescription.text.toString()

            if (ucuserName.isEmpty()) {
                binding.etHpuserName.setError("Name Required")
            }
            if (ucuserComplain.isEmpty()) {
                binding.etHpDescription.setError("Please explain the issue u are facing")
            }
            if (ucuserComplain.isNotEmpty() && ucuserName.isNotEmpty())
            {
                onSubmitBtnClick()
            }

        }

        return binding.root
    }

    private fun fetchUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("users").child(userId)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val name = if (dataSnapshot.exists()) {
                        val user = dataSnapshot.getValue(dataofUser::class.java)
                        binding.txtUserBalance.text =
                            "$" + user?.walletCurrentbalance.toString() + ".00"
                        binding.txtProfileName.text = user?.name.toString()
                        binding.txtprofileEmail.text = user?.email.toString()
                        binding.txtUserEmail.text = user?.email.toString()
                        binding.txtUserPhone.text = user?.phoneNumber.toString()
                        binding.txtUserID.text = user?.cnic.toString()
                        binding.etHpemail.text = user?.email.toString()

                    } else {
                        Toast.makeText(context, "User Data Not Found", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error: ${databaseError.message}")
                }
            }

            myRef.addListenerForSingleValueEvent(valueEventListener)
        } else {
            Toast.makeText(context, "Error occured please try again later", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun onToggleButtons(btnColor: Button) {


        // Reset colors for all buttons
        binding.btnHelpCenter.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_btn_toggle))
        binding.btnProfile.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_btn_toggle))


        // Reset colors for all text and icons


        binding.btnHelpCenter.setTextColor(Color.BLACK)
        binding.btnProfile.setTextColor(Color.BLACK)


        // Apply selected color
        btnColor.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_btn))
        btnColor.setTextColor(resources.getColor(R.color.white))

        when (btnColor) {
            binding.btnHelpCenter -> {
                binding.llProfileInfo.visibility = View.GONE
                binding.llHP.visibility = View.VISIBLE


            }

            binding.btnProfile -> {
                binding.llProfileInfo.visibility = View.VISIBLE
                binding.llHP.visibility = View.GONE
            }
        }
    }

    private fun uploadUserComplaint(
        userName: String,
        userEmail: String,
        userComplaint: String,
        complaintId: String,
        userId: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val complaintRequestsRef = database.getReference("User-Complains").child(userId)

        val userComplains = dataOfcomplains(userName, userEmail, userComplaint, complaintId, userId)

        complaintRequestsRef.setValue(userComplains)
            .addOnSuccessListener {
                // Request uploaded successfully
                Toast.makeText(requireContext(), "Issue Reported successfully", Toast.LENGTH_SHORT).show()
                // Clear EditText fields
                binding.etHpuserName.text.clear()
                binding.etHpDescription.text.clear()
            }
            .addOnFailureListener { e ->
                // Failed to upload request
                Toast.makeText(requireContext(), "Failed to report issue: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                // Hide progress bar when the operation completes (success or failure)
                binding.userProfileProg.visibility = View.GONE
            }
    }



    private fun onSubmitBtnClick() {
        // Get user input
        val userName = binding.etHpuserName.text.toString().trim()
        val userEmail = binding.etHpemail?.text.toString().trim() // Add null-check
        val userComplaint = binding.etHpDescription.text.toString().trim()

        // Validate input fields
        if (!validateFields(userName, userComplaint)) return

        // Show progress bar
        binding.userProfileProg.visibility = View.VISIBLE

        // Get current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Generate complaint ID
            val complaintId = "${userId}_${UUID.randomUUID()}"

            // Upload complaint to Firebase
            uploadUserComplaint(userName, userEmail, userComplaint, complaintId, userId)
        } else {
            showErrorToast("Error occurred. Please try again later.")
            binding.userProfileProg.visibility = View.GONE
        }
    }
    private fun validateFields(userName: String, userComplaint: String): Boolean {
        if (userName.isEmpty()) {
            binding.etHpuserName.error = "Name Required"
            return false
        }
        if (userComplaint.isEmpty()) {
            binding.etHpDescription.error = "Please explain the issue you are facing"
            return false
        }
        return true
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


}