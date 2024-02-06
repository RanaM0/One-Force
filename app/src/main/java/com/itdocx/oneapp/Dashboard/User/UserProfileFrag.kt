package com.itdocx.oneapp.Dashboard.User

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneapp.Models.dataofUser
import com.itdocx.oneapp.databinding.FragmentUserProfileBinding


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
                        binding.txtUserBalance.text = "$"+user?.walletCurrentbalance.toString()+".00"
                        binding.txtProfileName.text = user?.name.toString()
                        binding.txtprofileEmail.text = user?.email.toString()
                        binding.txtUserEmail.text = user?.email.toString()
                       binding.txtUserAddress.text = user?.address.toString()
                        binding.txtUserPhone.text = user?.phoneNumber.toString()
                        binding.txtUserID.text = user?.cnic.toString()

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


}