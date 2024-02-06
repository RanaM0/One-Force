package com.itdocx.oneapp.Dashboard.Admin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneapp.MainActivity
import com.itdocx.oneapp.Models.dataofUser
import com.itdocx.oneapp.R
import com.itdocx.oneapp.databinding.FragmentAdminBinding


class AdminFrag : Fragment() {

    private var _binding : FragmentAdminBinding? = null
    private val binding get() = _binding!!

    private var backPressedOnce = false
    private val doubleBackPressedInterval = 2000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAdminBinding.inflate(inflater, container, false)

        binding.rlAdminDepositReq.setOnClickListener {

            findNavController().navigate(R.id.action_adminFrag_to_depositRequests)

        }

        binding.rlAdminLogout.setOnClickListener{

            adminLogout()

        }

        fetchAdminData()

        return binding.root
    }

    private fun adminLogout() {

        val auth = FirebaseAuth.getInstance()


        auth.signOut()
        // Redirect to login activity or perform other actions as needed
        // For example:

        val intent = Intent(
            requireContext(),
            MainActivity::class.java
        )
        // Add flags to clear the activity stack
        // Add flags to clear the activity stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        // Finish current activity
        // Finish current activity
        requireActivity().finish()

    }


    private fun fetchAdminData(){

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("users").child(userId)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Check if the user data exists
                    if (dataSnapshot.exists()) {
                        val user = dataSnapshot.getValue(dataofUser::class.java)
                        // Do something with the user object
                        binding.txtAdminName.text = user?.name ?: "No Name Available"
                        binding.txtAdminEmail.text = user!!.email.toString()

                    } else {
                        // Handle case where user data doesn't exist
                        binding.txtAdminName.text = "User Data Not Found"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    println("Error: ${databaseError.message}")
                }
            }

            // Attach the listener to the database reference
            myRef.addListenerForSingleValueEvent(valueEventListener)
        } else {
            // Handle case where current user is null
            binding.txtAdminName.text = "No User Logged In"
        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (backPressedOnce) {
                requireActivity().finish()
            } else {
                backPressedOnce = true
                Toast.makeText(requireContext(), "Press back again to exit", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    backPressedOnce = false
                }, doubleBackPressedInterval.toLong())
            }
        }
    }

}