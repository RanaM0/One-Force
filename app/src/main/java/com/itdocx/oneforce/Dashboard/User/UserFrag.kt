package com.itdocx.oneforce.Dashboard.User

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneforce.Adapters.MyWDAdapter
import com.itdocx.oneforce.Models.dataofAnouncements
import com.itdocx.oneforce.Models.dataofUser
import com.itdocx.oneforce.Models.investmentData
import com.itdocx.oneforce.Models.withdrawReq
import com.itdocx.oneforce.databinding.FragmentUserBinding


class UserFrag : Fragment() {


    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    private var backPressedOnce = false
    private val doubleBackPressedInterval = 2000
    private var userBalance: String? = null

    val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private var adapter: MyWDAdapter? = null
    private var withdrawUserList: MutableList<withdrawReq>? = null

    private var isNewDataAvailable = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserBinding.inflate(inflater, container, false)

        fetchUserData()


        withdrawUserList = mutableListOf()
        fetchUserWithdrawReq(userId)
        setupSwipeRefreshLayout()

        binding.ivUserAnouncements.setOnClickListener {
            binding.cardUserAnouncements.visibility = View.VISIBLE
            fetchAnnouncement()
        }
        binding.ivAnouncementClose.setOnClickListener {
            binding.cardUserAnouncements.visibility = View.GONE
        }
        binding.cardInvest10.setOnClickListener {
            val userBalanceNumeric = userBalance?.toDoubleOrNull()
            // Convert userBalance to a numeric type (assuming it represents a monetary value)
            // Check if the conversion was successful and userBalance is greater than or equal to 10
            if (userBalanceNumeric != null && userBalanceNumeric >= 10) {
                val amounttoBeInvested = binding.txtInvest10rs.text.toString()

                onInvestmentButtonClick(amounttoBeInvested, "2.0")
            } else {
                // Handle the case where userBalance is not a valid numeric value or is less than 10
                Toast.makeText(
                    requireContext(),
                    "Insufficient balance for investment",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.cardInvest50.setOnClickListener {
            // Convert userBalance to a numeric type (assuming it represents a monetary value)
            val userBalanceNumeric50 = userBalance?.toDoubleOrNull()
            // Check if the conversion was successful and userBalance is greater than or equal to 10
            if (userBalanceNumeric50 != null && userBalanceNumeric50 >= 50.0) {
                val amounttoBeInvested50 = binding.txtInvest50rs.text.toString()

                onInvestmentButtonClick(amounttoBeInvested50, "2.0")
            } else {
                // Handle the case where userBalance is not a valid numeric value or is less than 10
                Toast.makeText(
                    requireContext(),
                    "Insufficient balance for investment",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.cardInvest100.setOnClickListener {
            // Convert userBalance to a numeric type (assuming it represents a monetary value)
            val userBalanceNumeric100 = userBalance?.toDoubleOrNull()
            // Check if the conversion was successful and userBalance is greater than or equal to 10
            if (userBalanceNumeric100 != null && userBalanceNumeric100 >= 100) {
                val amounttoBeInvested100 = binding.txtInvest100rs.text.toString()

                onInvestmentButtonClick(amounttoBeInvested100, "3.0")
            } else {
                // Handle the case where userBalance is not a valid numeric value or is less than 10
                Toast.makeText(
                    requireContext(),
                    "Insufficient balance for investment",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.cardInvest200.setOnClickListener {
            // Convert userBalance to a numeric type (assuming it represents a monetary value)
            val userBalanceNumeric200 = userBalance?.toDoubleOrNull()
            // Check if the conversion was successful and userBalance is greater than or equal to 10
            if (userBalanceNumeric200 != null && userBalanceNumeric200 >= 200) {
                val amounttoBeInvested200 = binding.txtInvest200rs.text.toString()

                onInvestmentButtonClick(amounttoBeInvested200, "4.0")
            } else {
                // Handle the case where userBalance is not a valid numeric value or is less than 10
                Toast.makeText(
                    requireContext(),
                    "Insufficient balance for investment",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.cardInvest500.setOnClickListener {
            // Convert userBalance to a numeric type (assuming it represents a monetary value)
            val userBalanceNumeric500 = userBalance?.toDoubleOrNull()
            // Check if the conversion was successful and userBalance is greater than or equal to 10
            if (userBalanceNumeric500 != null && userBalanceNumeric500 >= 500) {
                val amounttoBeInvested500 = binding.txtInvest500rs.text.toString()

                onInvestmentButtonClick(amounttoBeInvested500, "5.0")
            } else {
                // Handle the case where userBalance is not a valid numeric value or is less than 10
                Toast.makeText(
                    requireContext(),
                    "Insufficient balance for investment",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.cardInvest1000.setOnClickListener {
            // Convert userBalance to a numeric type (assuming it represents a monetary value)
            val userBalanceNumeric1000 = userBalance?.toDoubleOrNull()
            // Check if the conversion was successful and userBalance is greater than or equal to 10
            if (userBalanceNumeric1000 != null && userBalanceNumeric1000 >= 1000) {
                val amounttoBeInvested1000 = binding.txtInvest1000rs.text.toString()

                onInvestmentButtonClick(amounttoBeInvested1000, "6.0")
            } else {
                // Handle the case where userBalance is not a valid numeric value or is less than 10
                Toast.makeText(
                    requireContext(),
                    "Insufficient balance for investment",
                    Toast.LENGTH_SHORT
                ).show()
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

                        binding.txtDashUserName.text = "Hi " + user!!.name.toString()
                        userBalance = user.walletCurrentbalance.toString()

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
        profit: String,
        balance: String,
        userId: String
    ) {

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val depositRequestsRef = FirebaseDatabase.getInstance().getReference("User-Investments")
            val requestId = depositRequestsRef.push().key // Generate a unique key for the request

            if (requestId != null) {
                // Generate a unique investment ID using timestamp and user's UID
                val timestamp = System.currentTimeMillis()
                val investId = "${currentUser.uid}_$timestamp"

                // Create a deposit request object
                val depositRequest = investmentData(
                    name,
                    email,
                    investment,
                    cycle,
                    profit,
                    balance,
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

                        val userbalance = dataSnapshot.getValue(dataofUser::class.java)

                        // Deduct investment amount from current balance
                        val balance =
                            userbalance?.walletCurrentbalance.toString().replace(Regex("[^0-9.]"), "").toDoubleOrNull()
                                ?: 0.00
//                        val currentBalance = balance
                        val investmentAmount =
                            amount.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.00
                        val newBalance = balance - investmentAmount

                        if (newBalance >= 0) {
                            // Update balance in the database
                            val updatedBalance = newBalance.toString()
                            myRef.child("walletCurrentbalance").setValue(updatedBalance)

                            // Once the image is uploaded, imageURL is available, use it to send the request
                            onInvestmentCreated(
                                username,
                                userEmail,
                                investments,
                                cycle,
                                investments,
                                updatedBalance,
                                userId
                            )

//                            Toast.makeText(
//                                requireContext(),
//                                "Investment created successfully!",
//                                Toast.LENGTH_SHORT
//                            ).show()
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


    private fun fetchUserWithdrawReq(currentUserUid: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Withdraw-Requests")
        val query = databaseRef.orderByChild("uid").equalTo(currentUserUid)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                withdrawUserList?.clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(withdrawReq::class.java)
                    user?.let {
                        withdrawUserList?.add(it)
                    }

                }

                if (withdrawUserList!!.isNotEmpty()) {
                    binding.txtUserWDrvPlaceholder.visibility = View.GONE
                    binding.rvuserWDReqList.visibility = View.VISIBLE
                    binding.rvuserWDReqList.layoutManager = LinearLayoutManager(requireContext())
                    adapter = MyWDAdapter(requireContext(), withdrawUserList!!)
                    binding.rvuserWDReqList.adapter = adapter
                    adapter?.notifyDataSetChanged()
                } else {
                    binding.txtUserWDrvPlaceholder.visibility = View.VISIBLE
                    binding.rvuserWDReqList.visibility = View.GONE
                }

                // Hide refreshing indicator
                binding.userWDRvRefresh.isRefreshing = false
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Hide refreshing indicator
                binding.userWDRvRefresh.isRefreshing = false
                Toast.makeText(
                    requireContext(),
                    "Failed to retrieve investments",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun fetchAnnouncement() {
        val announcementsRef = FirebaseDatabase.getInstance().getReference("Announcements")
        // Replace your_button_id with the actual ID of your button

        announcementsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the latest announcement data
                    val announcementData = dataSnapshot.getValue(dataofAnouncements::class.java)
                    if (announcementData != null) {
                        // Handle the announcement data here
                        binding.txtAnnouncementTitle.text = announcementData.title
                        binding.txtAnnouncement.text = announcementData.anouncements

                        // Check if new data is available and change button color accordingly
                        if (!isNewDataAvailable) {
                            // Change the button color to indicate new data
                            binding.ivUserAnouncements.setColorFilter(Color.RED) // Change to your desired color
                            isNewDataAvailable = true // Set flag to true
                        }
                    }
                } else {
                    // Handle case where no announcement exists
                    // You can change the button color back to its default state here
                    binding.ivUserAnouncements.setColorFilter(Color.BLUE) // Change to your default color
                    isNewDataAvailable = false // Set flag to false as no new data
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Toast.makeText(
                    requireContext(),
                    "Database error: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Set an OnClickListener to reset button color when user interacts with the data
            // Change the button color back to its default state
            binding.ivUserAnouncements.setColorFilter(Color.WHITE) // Change to your default color

    }

    private fun setupSwipeRefreshLayout() {
        binding.userWDRvRefresh.setOnRefreshListener {
            fetchUserWithdrawReq(userId)
            binding.userWDRvRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}