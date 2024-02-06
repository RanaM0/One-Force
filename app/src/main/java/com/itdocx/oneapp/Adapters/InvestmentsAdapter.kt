package com.itdocx.oneapp.Adapters

import android.content.Context
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
import com.itdocx.oneapp.Models.investmentData
import com.itdocx.oneapp.R

class InvestmentsAdapter(
    private val context: Context,
    private val investmentList: MutableList<investmentData>
) : RecyclerView.Adapter<InvestmentsAdapter.ViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())
    private val profitUpdateIntervalMillis = 10000L // 10 seconds
    private val totalProfitPercent = 0.02 // 2%

    // Map to store investment amounts and their corresponding scheduled profit updates
    private val scheduledUpdatesMap = mutableMapOf<String, Runnable>()



    class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {

        val txtName = itemview.findViewById<TextView>(R.id.InvestorName)
        val txtAmount = itemview.findViewById<TextView>(R.id.txtInvestmentAmountTitle)
        val txtEmail = itemview.findViewById<TextView>(R.id.InvestorEmail)
        val txtInvestment = itemview.findViewById<TextView>(R.id.InvestorBalance)
        val txtCycle = itemview.findViewById<TextView>(R.id.profitCycle)
        val btnStop = itemview.findViewById<Button>(R.id.btnstopCycle)

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

        holder.btnStop.setOnClickListener {
            val profitAMOUNT =
                holder.txtAmount.text.toString().replace(Regex("[^0-9.]"), "").toDoubleOrNull()
                    ?: 0.0
            removeInvestment(listItems.investmentId, profitAMOUNT)
        }

        //Get current User Investments
        getUserInvestmentForCurrentUser(listItems.userid)


        // Calculate profit and schedule profit updates only if investment amount is $10
        if (listItems.investment == "$10") {
            val investmentAmount =
                listItems.investment.replace(Regex("[^0-9.]"), "").toDoubleOrNull()


            // Schedule profit updates only if investment amount is valid
            if (investmentAmount != null) {
                if (investmentAmount >= 10.0) {
                    //scheduleProfitUpdates(holder, investmentAmount, listItems.investmentId)
                }
            }
        }
    }


    private fun getUserInvestmentForCurrentUser(investmentId: String) {
        val balanceRef = FirebaseDatabase.getInstance().getReference("User-Investments")
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

        currentUser?.let { user ->
            val query = balanceRef.child("userid").equalTo(user)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val investment = snapshot.getValue(investmentData::class.java)
                            if (investment?.userid == investmentId) {
                                // Here you can handle the found investment for the current user
                                // For example, you can show it in a toast
                                Toast.makeText(
                                    context,
                                    "Investment found for the current user: ${investment.toString()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // You can also return the investment or do any other processing here
                                // return
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



    private fun removeInvestment(uid: String, profit: Double) {
        val depositRequestsRef = FirebaseDatabase.getInstance().getReference("User-Investments")
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





}
