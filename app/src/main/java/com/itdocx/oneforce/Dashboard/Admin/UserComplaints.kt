package com.itdocx.oneforce.Dashboard.Admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.itdocx.oneforce.Adapters.UserComplaintsAdapter
import com.itdocx.oneforce.Models.dataOfcomplains
import com.itdocx.oneforce.databinding.FragmentUserComplaintsBinding


class UserComplaints : Fragment() {

    private var _binding: FragmentUserComplaintsBinding? = null
    private val binding get() = _binding!!

    private var listComplains: MutableList<dataOfcomplains>? = null
    private var complainadapter: UserComplaintsAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserComplaintsBinding.inflate(inflater, container, false)


        listComplains = mutableListOf()

        binding.adminUserComplainsSRL.setOnRefreshListener {
            fetchUserComplaints()

            binding.adminUserComplainsSRL.isRefreshing = false
        }




        fetchUserComplaints()

        oncomplainSearchAction()




        return binding.root
    }


    private fun fetchUserComplaints() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("User-Complains")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(dataOfcomplains::class.java)
                    user?.let {
                        listComplains?.add(it)
                    }
                }

                if (listComplains?.isNotEmpty() == true) {
                    binding.txtadminUserrvPlaceholder.visibility = View.GONE
                    binding.rvuserComplains.visibility = View.VISIBLE
                    binding.rvuserComplains.layoutManager = LinearLayoutManager(requireContext())
                    complainadapter = UserComplaintsAdapter(requireContext(), listComplains!!)
                    binding.rvuserComplains.adapter = complainadapter
                } else {
                    binding.txtadminUserrvPlaceholder.visibility = View.VISIBLE
                    binding.rvuserComplains.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Failed to retrieve users data: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun oncomplainSearchAction() {
        binding.etcomplainSearchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // No implementation needed here
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
        })
    }

    private fun filterUsers(query: String) {
        val filteredList = listComplains?.filter {
            it.name.contains(query, ignoreCase = true) ?: false
        }
        complainadapter?.filterList(filteredList.orEmpty())
    }


}