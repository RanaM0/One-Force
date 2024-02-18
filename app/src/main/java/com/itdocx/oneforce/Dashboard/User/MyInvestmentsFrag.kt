package com.itdocx.oneforce.Dashboard.User

import ProfitUpdateService
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneforce.Adapters.InvestmentsAdapter
import com.itdocx.oneforce.Models.investmentData
import com.itdocx.oneforce.databinding.FragmentMyInvestmentsBinding


class MyInvestmentsFrag : Fragment(), InvestmentsAdapter.ProfitUpdateListener {

    private var _binding: FragmentMyInvestmentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: InvestmentsAdapter
    private val userList = mutableListOf<investmentData>()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    private var isServiceScheduled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyInvestmentsBinding.inflate(inflater, container, false)
        setupRecyclerView()
        fetchMyInvestments(currentUserUid)
        binding.investmentRefresh.setOnRefreshListener {
            setupRecyclerView()
            fetchMyInvestments(currentUserUid)
            binding.investmentRefresh.isRefreshing = false
        }
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvMyInvestments.layoutManager = LinearLayoutManager(requireContext())
        adapter = InvestmentsAdapter(requireContext(), userList, this)
        binding.rvMyInvestments.adapter = adapter
    }

    private fun fetchMyInvestments(currentUserUid: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("User-Investments")
        val query = databaseRef.orderByChild("userid").equalTo(currentUserUid)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(investmentData::class.java)
                    user?.let { userList.add(it) }
                }

                if (userList.isNotEmpty()) {
                    binding.txtinvestmentPlaceHolder.visibility = View.GONE
                    binding.rvMyInvestments.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged() // Update adapter with new data

                    // Schedule service if not already scheduled
                    if (!isServiceScheduled) {
                        scheduleService(requireContext())
                        isServiceScheduled = true
                    }
                } else {
                    binding.txtinvestmentPlaceHolder.visibility = View.VISIBLE
                    binding.rvMyInvestments.visibility = View.GONE

                    // Cancel scheduled service
                    cancelScheduledService(requireContext())
                    isServiceScheduled = false
                }
                binding.investmentRefresh.isRefreshing = false
            }

            override fun onCancelled(databaseError: DatabaseError) {
                binding.investmentRefresh.isRefreshing = false
                Toast.makeText(
                    requireContext(),
                    "Failed to retrieve investments",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun scheduleService(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ProfitUpdateService::class.java)
        val pendingIntent = PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the service to run every 30 minutes
        val intervalMillis = 10000 // 30 minutes
        val triggerAtMillis = System.currentTimeMillis() + intervalMillis

        try {
            // Use setExactAndAllowWhileIdle to ensure exact triggering even in idle mode
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )

            // Store information about the scheduled alarm in SharedPreferences
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putLong("alarm_trigger_time", triggerAtMillis)
                apply()
            }
        } catch (e: SecurityException) {
            // Handle SecurityException
            e.printStackTrace()
            // Log or show a message indicating that setting exact alarms is not allowed
            // You may fallback to a less precise mechanism or inform the user accordingly
        }
    }


    private fun cancelScheduledService(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ProfitUpdateService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }

        // Clear the stored alarm information from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("alarm_trigger_time")
            apply()
        }
    }

    override fun onProfitUpdated(newProfit: String) {
        // Notify the RecyclerView adapter that profits are updated
        view?.post {
            // Update the adapter data or perform any UI-related operations here
            adapter.notifyDataSetChanged()
        }
        // You can also perform any other UI updates here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}