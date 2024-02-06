package com.itdocx.oneapp.Dashboard.User

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneapp.Adapters.InvestmentsAdapter
import com.itdocx.oneapp.Models.investmentData
import com.itdocx.oneapp.databinding.FragmentMyInvestmentsBinding


class MyInvestmentsFrag : Fragment() {

    private var _binding:FragmentMyInvestmentsBinding?=null
    private val binding get() = _binding!!

    private lateinit var adapter: InvestmentsAdapter
    private var userList: MutableList<investmentData> = mutableListOf()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding= FragmentMyInvestmentsBinding.inflate(inflater, container, false)


        setupSwipeRefreshLayout()
        fetchMyInvestments()

        return binding.root
    }

    private fun setupSwipeRefreshLayout() {
        binding.investmentRefresh.setOnRefreshListener {
            fetchMyInvestments()
        }
    }



    private fun fetchMyInvestments(){
    val databaseRef = FirebaseDatabase.getInstance().getReference("User-Investments")
    databaseRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            userList.clear()
            for (snapshot in dataSnapshot.children) {
                val user = snapshot.getValue(investmentData::class.java)
                user?.let {
                    userList.add(it)
                }
            }

            if (userList.isNotEmpty()) {
                binding.txtinvestmentPlaceHolder.visibility = View.GONE
                binding.rvMyInvestments.visibility = View.VISIBLE
                binding.rvMyInvestments.layoutManager = LinearLayoutManager(requireContext())
                adapter = InvestmentsAdapter(requireContext(), userList)
                binding.rvMyInvestments.adapter = adapter

                adapter.notifyDataSetChanged()
            } else {
                binding.txtinvestmentPlaceHolder.visibility = View.VISIBLE
                binding.rvMyInvestments.visibility = View.GONE
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Toast.makeText(requireContext(), "Failed to retrieve deposit requests", Toast.LENGTH_SHORT).show()
        }
    })




}



}