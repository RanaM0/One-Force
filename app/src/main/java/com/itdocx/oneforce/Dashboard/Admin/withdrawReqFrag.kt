package com.itdocx.oneforce.Dashboard.Admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneforce.Adapters.WithdrawReqAdapter
import com.itdocx.oneforce.Models.withdrawReq
import com.itdocx.oneforce.R
import com.itdocx.oneforce.databinding.FragmentWithdrawReqBinding


class withdrawReqFrag : Fragment() {


    private var _binding: FragmentWithdrawReqBinding? = null
    private val binding get() = _binding!!

    private var withdrawList: MutableList<withdrawReq>? = null
    private var adapter: WithdrawReqAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentWithdrawReqBinding.inflate(inflater, container, false)

        Glide.with(this).asGif().load(R.drawable.anim_deposit_list).into(binding.ivWithdrawAnim)

        binding.rvWithdrawList.layoutManager = LinearLayoutManager(requireContext())
        adapter = WithdrawReqAdapter(
            requireContext(),
            mutableListOf()
        ) // Initialize adapter with an empty list
        binding.rvWithdrawList.adapter = adapter

        binding.adminWDSR.setOnRefreshListener {
            // Fetch withdrawal requests when refreshing
            fetchMyWithdrawReq()
            binding.adminWDSR.isRefreshing = false // Stop the refreshing animation
        }

        binding.clWithdrawBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        // Fetch withdrawal requests
        fetchMyWithdrawReq()

        return binding.root
    }


    private fun fetchMyWithdrawReq() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Withdraw-Requests")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                withdrawList = mutableListOf() // Initialize withdrawList
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(withdrawReq::class.java)
                    user?.let {
                        withdrawList?.add(it)
                    }
                }

                if (withdrawList.isNullOrEmpty()) {
                    binding.txtWithdrawrvPlaceholder.visibility = View.VISIBLE
                    binding.rvWithdrawList.visibility = View.GONE
                } else {
                    binding.txtWithdrawrvPlaceholder.visibility = View.GONE
                    binding.rvWithdrawList.visibility = View.VISIBLE
                    adapter?.updateData(withdrawList!!) // Update adapter data
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Failed to retrieve deposit requests",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}