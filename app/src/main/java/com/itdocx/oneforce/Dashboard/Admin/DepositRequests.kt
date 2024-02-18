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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneforce.Adapters.RequestAdapter
import com.itdocx.oneforce.Models.UserRequests
import com.itdocx.oneforce.R
import com.itdocx.oneforce.databinding.FragmentDepositRequestsBinding


class DepositRequests : Fragment() {

    private var _binding: FragmentDepositRequestsBinding? = null
    private val binding get() = _binding!!
    private var adapter: RequestAdapter? = null
    private var userList: MutableList<UserRequests>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDepositRequestsBinding.inflate(inflater, container, false)

        Glide.with(this).asGif().load(R.drawable.anim_deposit_list).into(binding.ivDepositAnim)

        binding.clBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.rvDepositList.layoutManager = LinearLayoutManager(requireContext())
        userList = mutableListOf()
        adapter = RequestAdapter(requireContext(), userList!!)
        binding.rvDepositList.adapter = adapter

        binding.adminDRSR.setOnRefreshListener {

            fetchUserData()

            binding.adminDRSR.isRefreshing = false
        }

        fetchUserData()
        return binding.root
    }

    private fun fetchUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val database = FirebaseDatabase.getInstance().getReference("DepositRequests")

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val user = snapshot.getValue(UserRequests::class.java)
                            user?.let {
                                userList?.add(it)
                            }
                        }
                        adapter?.notifyDataSetChanged()
                        binding.rvDepositList.visibility = View.VISIBLE
                        binding.txtrvPlaceholder.visibility = View.GONE
                    } else {
                        // Hide RecyclerView and show empty list message
                        binding.rvDepositList.visibility = View.GONE
                        binding.txtrvPlaceholder.visibility = View.VISIBLE
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error: ${databaseError.message}")
                }
            }

            database.addListenerForSingleValueEvent(valueEventListener)
        } else {
            Toast.makeText(context, "Error occurred please try again later", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
