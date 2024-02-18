package com.itdocx.oneforce.Adapters

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneforce.Dashboard.User.MyInvestmentsFrag
import com.itdocx.oneforce.Models.dataofUser
import com.itdocx.oneforce.Models.investmentData
import com.itdocx.oneforce.R


class InvestmentsAdapter(
    private val context: Context,
    private val investmentList: MutableList<investmentData>,
    private val profitUpdateListener: MyInvestmentsFrag
) : RecyclerView.Adapter<InvestmentsAdapter.ViewHolder>() {


    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    // Define keys for SharedPreferences
    private val KEY_LAST_UPDATE_TIME = "last_update_time"
    private val KEY_CURRENT_NET_PROFIT = "current_net_profit"

    inner class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val txtName: TextView = itemview.findViewById(R.id.InvestorName)
        val txtAmount: TextView = itemview.findViewById(R.id.txtInvestmentAmountTitle)
        val txtEmail: TextView = itemview.findViewById(R.id.InvestorEmail)
        val txtInvestment: TextView = itemview.findViewById(R.id.InvestorBalance)
        val txtCycle: TextView = itemview.findViewById(R.id.profitCycle)
        val btnStop: Button = itemview.findViewById(R.id.btnstopCycle)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_investments_listitems, parent, false)


        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return investmentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listItems = investmentList[position]
        holder.txtName.text = listItems.name
        holder.txtEmail.text = listItems.email
        holder.txtInvestment.text = listItems.investment
        holder.txtCycle.text = listItems.cycle
        holder.txtAmount.text = listItems.profit

        val investmentAmount =
            listItems.investment.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0


        holder.btnStop.setOnClickListener {
            val profitAMOUNT = listItems.profit.replace(Regex("[^0-9.]"), "").toDoubleOrNull()
                ?: 0.0 // Use the profit directly

            val balanceofUser = listItems.balance.replace(Regex("[^0-9.]"), "").toDoubleOrNull()
                ?: 0.0 // Ensure balance is converted to Double safely

            val profitToDeposit = profitAMOUNT + balanceofUser
            val depositProfit = profitToDeposit.toString()
            updateUserBalanceData(depositProfit, listItems.userid)
            removeInvestment(listItems.investmentId)

        }


                scheduleProfitUpdates(holder,listItems)
            }



    private fun removeInvestment(uid: String) {
        val depositRequestsRef =
            FirebaseDatabase.getInstance().getReference("User-Investments")
        val query = depositRequestsRef.orderByChild("investmentId").equalTo(uid)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                // Add the profit amount to the user's balance when investment is stopped
                                //   addToUserBalance(profit)


                                Toast.makeText(
                                    context,
                                    "Investment has been stopped successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Failed to remove deposit request: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Deposit request does not exist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    context,
                    "Database error: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    private fun updateUserBalanceData(currentBalance: String, id: String) {
        val balanceRef = FirebaseDatabase.getInstance().getReference("users")
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val query = balanceRef.orderByChild("uid").equalTo(id)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val balanceData = snapshot.getValue(dataofUser::class.java)
                            if (balanceData != null) {
                                // Update the existing balance data
                                balanceData.walletCurrentbalance = currentBalance


                                // Save the updated balance data
                                snapshot.ref.setValue(balanceData)
                                    .addOnSuccessListener {
                                        // Balance data updated successfully
                                        // You can perform any additional actions here if needed
                                        Toast.makeText(
                                            context,
                                            "Funds Deposited Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        // Failed to update balance data
                                        Toast.makeText(
                                            context,
                                            "Failed to update balance data: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    } else {
                        // Handle case where balance data for the user doesn't exist
                        Toast.makeText(
                            context,
                            "Balance data for the user does not exist",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    Toast.makeText(
                        context,
                        "Database error: ${databaseError.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }


    private fun scheduleProfitUpdates(holder: ViewHolder, investmentData: investmentData) {
        val investAmount = (investmentData.investment.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0)
        val cycle = when (investAmount) {
            10.0 -> 0.00034722
            50.0 -> 0.00034722
            100.0 -> 0.0000104167
            200.0 -> 0.00027777
            500.0 -> 0.00028935
            1000.0 -> 0.0006944
            else -> return
        }

        val lastUpdateTime =
            sharedPreferences.getLong(KEY_LAST_UPDATE_TIME + investmentData.investmentId, System.currentTimeMillis())
        val currentNetProfit =
            sharedPreferences.getFloat(KEY_CURRENT_NET_PROFIT + investmentData.investmentId, 0.02f)

        val profitUpdateHandler = Handler(Looper.getMainLooper())
        profitUpdateHandler.postDelayed({

            val invest = investmentData.investment.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
            val profit = invest * cycle
            val newNetProfit = currentNetProfit + profit

            holder.txtAmount.text = "$$newNetProfit"
            updateProfitInDatabase(newNetProfit.toString(), investmentData.investmentId)
            profitUpdateListener.onProfitUpdated(newNetProfit.toString())

            saveUpdateInfoToSharedPreferences(investmentData.investmentId, newNetProfit.toFloat(), System.currentTimeMillis())

            scheduleProfitUpdates(holder, investmentData)

        }, 30000) // 30 seconds delay
    }

    private fun saveUpdateInfoToSharedPreferences(investmentId: String, currentNetProfit: Float, updateTime: Long) {
        sharedPreferences.edit().apply {
            putFloat(KEY_CURRENT_NET_PROFIT + investmentId, currentNetProfit)
            putLong(KEY_LAST_UPDATE_TIME + investmentId, updateTime)
            apply()
        }
    }


    private fun updateProfitInDatabase(profit: String, investmentId: String) {
        val investmentsRef =
            FirebaseDatabase.getInstance().getReference("User-Investments")
        val query = investmentsRef.orderByChild("investmentId").equalTo(investmentId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        // Update the profit field in the database
                        snapshot.ref.child("profit").setValue(profit)
                            .addOnSuccessListener {
                                // Profit updated successfully
                            }
                            .addOnFailureListener { e ->
                                // Failed to update profit
                                Toast.makeText(
                                    context,
                                    "Failed to update profit: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Toast.makeText(
                    context,
                    "Database error: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    interface ProfitUpdateListener {
        fun onProfitUpdated(newProfit: String)
    }
}
