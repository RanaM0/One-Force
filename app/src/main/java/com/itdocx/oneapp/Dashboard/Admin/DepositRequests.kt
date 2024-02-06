package com.itdocx.oneapp.Dashboard.Admin

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
import com.itdocx.oneapp.Adapters.RequestAdapter
import com.itdocx.oneapp.Models.UserRequests
import com.itdocx.oneapp.R
import com.itdocx.oneapp.databinding.FragmentDepositRequestsBinding


class DepositRequests : Fragment() {

    private var _binding: FragmentDepositRequestsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RequestAdapter
    private var userList: MutableList<UserRequests> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDepositRequestsBinding.inflate(inflater, container, false)

        Glide.with(this).asGif().load(R.drawable.anim_deposit_list).into(binding.ivDepositAnim)

        fetchDepositReq()


        binding.clBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }



        return binding.root
    }


    private fun fetchDepositReq() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("DepositRequests")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(UserRequests::class.java)
                    user?.let {
                        userList.add(it)
                    }
                }

                if (userList.isNotEmpty()) {
                    binding.txtrvPlaceholder.visibility = View.GONE
                    binding.rvDepositList.visibility = View.VISIBLE
                    binding.rvDepositList.layoutManager = LinearLayoutManager(requireContext())
                    adapter = RequestAdapter(requireContext(), userList)
                    binding.rvDepositList.adapter = adapter
                    adapter.notifyDataSetChanged()
                } else {
                    binding.txtrvPlaceholder.visibility = View.VISIBLE
                    binding.rvDepositList.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to retrieve deposit requests", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}