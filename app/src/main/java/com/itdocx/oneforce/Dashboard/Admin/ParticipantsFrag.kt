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
import com.itdocx.oneforce.Adapters.AllUsersAdapter
import com.itdocx.oneforce.Models.dataofUser
import com.itdocx.oneforce.databinding.FragmentParticipantsBinding


class ParticipantsFrag : Fragment() {

    private var _binding: FragmentParticipantsBinding? = null
    private val binding get() = _binding!!

    private var AdminUsersList: MutableList<dataofUser>? = null
    private var adapter: AllUsersAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentParticipantsBinding.inflate(inflater, container, false)




        binding.adminUsersSRL.setOnRefreshListener {
            // Fetch withdrawal requests when refreshing
            fetchAllusersData()
            binding.adminUsersSRL.isRefreshing = false // Stop the refreshing animation
        }

        fetchAllusersData()

        onSearchAction()

        return binding.root
    }


    private fun fetchAllusersData() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("users")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
               AdminUsersList = mutableListOf() // Create a new list
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(dataofUser::class.java)
                    user?.let {
                        AdminUsersList?.add(it)
                    }
                }

                if (AdminUsersList?.isNotEmpty() == true) {
                    binding.txtadminUserrvPlaceholder.visibility = View.GONE
                    binding.rvAdminAllUsers.visibility = View.VISIBLE
                    binding.rvAdminAllUsers.layoutManager = LinearLayoutManager(requireContext())
                   adapter = AllUsersAdapter(requireContext(), AdminUsersList!!)
                    binding.rvAdminAllUsers.adapter = adapter
                } else {
                    binding.txtadminUserrvPlaceholder.visibility = View.VISIBLE
                    binding.rvAdminAllUsers.visibility = View.GONE
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

    private fun onSearchAction(){

        binding.etSearchBar.addTextChangedListener(object : TextWatcher {
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
        val filteredList = AdminUsersList?.filter {
            it.name?.contains(query, ignoreCase = true) ?: false
        }
        adapter?.filterList(filteredList.orEmpty())
    }


}