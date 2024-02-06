package com.itdocx.oneapp.Dashboard.User

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneapp.Models.dataofUser
import com.itdocx.oneapp.Models.investmentData
import com.itdocx.oneapp.databinding.FragmentUserBinding


class UserFrag : Fragment() {


    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    private var backPressedOnce = false
    private val doubleBackPressedInterval = 2000
    private var userBalance: String?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserBinding.inflate(inflater, container, false)

        fetchUserData()

        binding.cardInvest10.setOnClickListener {
            // Convert userBalance to a numeric type (assuming it represents a monetary value)
            val userBalanceNumeric = userBalance?.toDoubleOrNull()

            // Check if the conversion was successful and userBalance is greater than or equal to 10
            if (userBalanceNumeric != null && userBalanceNumeric >= 10) {
                val amounttoBeInvested = binding.txtInvest10rs.text.toString()



                onInvestmentButtonClick(amounttoBeInvested, "2.0")
            } else {
                // Handle the case where userBalance is not a valid numeric value or is less than 10
                Toast.makeText(requireContext(), "Insufficient balance for investment", Toast.LENGTH_SHORT).show()
            }
        }




        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (backPressedOnce) {
                requireActivity().finish()
            } else {
                backPressedOnce = true
                Toast.makeText(requireContext(), "Press back again to exit", Toast.LENGTH_SHORT)
                    .show()
                Handler(Looper.getMainLooper()).postDelayed({
                    backPressedOnce = false
                }, doubleBackPressedInterval.toLong())
            }
        }
    }


    private fun fetchUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("users").child(userId)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val name = if (dataSnapshot.exists()) {
                        val user = dataSnapshot.getValue(dataofUser::class.java)

                        binding.txtDashUserName.text = user!!.name.toString()
                        userBalance=user.walletCurrentbalance.toString()

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

    private fun onInvestmentCreated(
        name: String,
        email: String,
        investment: String,
        cycle: String,
        profit:String,
        userId: String
    ) {

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val depositRequestsRef = FirebaseDatabase.getInstance().getReference("User-Investments")
            val requestId = depositRequestsRef.push().key // Generate a unique key for the request

            if (requestId != null) {
                // Generate a unique investment ID using timestamp and user's UID
                val timestamp = System.currentTimeMillis()
                 val investId= "${currentUser.uid}_$timestamp"

                // Create a deposit request object
                val depositRequest = investmentData(
                    name,
                    email,
                    investment,
                    cycle,
                    profit,
                    investId,
                    userId
                )

                // Upload the deposit request to the database
                depositRequestsRef.child(requestId).setValue(depositRequest)
                    .addOnSuccessListener {
                        // Request uploaded successfully
                        // You can perform any additional actions here if needed
                        Toast.makeText(
                            requireContext(),
                            "Investment successful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        // Failed to upload request
                        Toast.makeText(
                            requireContext(),
                            "Failed to invest: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun onInvestmentButtonClick(amount: String, cycle: String) {

        binding.userProg.visibility = View.VISIBLE
        binding.llPlans.visibility = View.GONE
        // Replace with actual user name
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("users").child(userId)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Check if the user data exists
                    if (dataSnapshot.exists()) {
                        val user = dataSnapshot.getValue(investmentData::class.java)
                        // Do something with the user object
                        val username = user?.name ?: "No Name Available"
                        val userEmail = user?.email.toString()
                        val investments = amount
                        val cycle = cycle

                        // Deduct investment amount from current balance
                        val balance =userBalance.toString().replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.00
//                        val currentBalance = balance
                        val investmentAmount = amount.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.00
                        val newBalance = balance - investmentAmount

                        if (newBalance >= 0) {
                            // Update balance in the database
                            myRef.child("walletCurrentbalance").setValue(newBalance.toString())

                            // Once the image is uploaded, imageURL is available, use it to send the request
                            onInvestmentCreated(
                                username,
                                userEmail,
                                investments,
                                cycle,
                                investments,
                                userId
                            )

                            Toast.makeText(
                                requireContext(),
                                "Investment created successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Insufficient balance to make this investment!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong please try again later!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.userProg.visibility = View.GONE
                    binding.llPlans.visibility = View.VISIBLE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                    binding.userProg.visibility = View.GONE
                    binding.llPlans.visibility = View.VISIBLE
                }
            }

            // Attach the listener to the database reference
            myRef.addListenerForSingleValueEvent(valueEventListener)
        }
    }



}