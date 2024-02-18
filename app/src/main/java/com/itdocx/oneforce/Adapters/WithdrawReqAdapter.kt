package com.itdocx.oneforce.Adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneforce.Models.withdrawReq
import com.itdocx.oneforce.R

class WithdrawReqAdapter(val context: Context, val withdrawList: MutableList<withdrawReq>) :
    RecyclerView.Adapter<WithdrawReqAdapter.ViewHolder>() {

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtWDName = itemView.findViewById<TextView>(R.id.userWDName)
        val txtWDEmail = itemView.findViewById<TextView>(R.id.userWDEmail)
        val txtWDAccno = itemView.findViewById<TextView>(R.id.userWDAccountNo)
        val txtWDBank = itemView.findViewById<TextView>(R.id.userWDbankName)
        val txtWDStatus = itemView.findViewById<TextView>(R.id.userWDstatus)
        val txtWDAmount = itemView.findViewById<TextView>(R.id.txtWDAmountTitle)
        val txtWDCB = itemView.findViewById<TextView>(R.id.userWDBalance)
        val btnWDApproveReq = itemView.findViewById<Button>(R.id.btnWDApprove)
        val btnWDRejectReq = itemView.findViewById<Button>(R.id.btnWDReject)
        val btnWDClosReq = itemView.findViewById<ImageView>(R.id.ivCloseReq)


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WithdrawReqAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.withdraw_list_items,parent,false)


        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: WithdrawReqAdapter.ViewHolder, position: Int) {
        val listWD = withdrawList[position]
        holder.txtWDName.text = listWD.usrName
        holder.txtWDEmail.text = listWD.emale
        holder.txtWDAccno.text = listWD.accountNumber
        holder.txtWDBank.text = listWD.bankName
        holder.txtWDStatus.text = listWD.userReqStatus
        holder.txtWDCB.text = listWD.currentBalance
        holder.txtWDAmount.text = "$ "+listWD.WithdrawAmount.toString()

        holder.btnWDClosReq.setOnClickListener {

            removeWDReq(listWD.withdrawId)
        }


        holder.btnWDRejectReq.setOnClickListener {

            updateUserBalanceData(listWD.WithdrawAmount!!,"Canceled",listWD.withdrawId)
            holder.txtWDStatus.setTextColor(Color.RED)
        }

        holder.btnWDApproveReq.setOnClickListener {

            updateUserBalanceData(listWD.WithdrawAmount!!,"Approved",listWD.withdrawId)
            holder.txtWDStatus.setTextColor(Color.GREEN)

        }




    }

    override fun getItemCount(): Int {
        return withdrawList.size

    }

    private fun updateUserBalanceData(withdrawAmount:String,ApplicationStatus: String, id: String) {
        val balanceRef = FirebaseDatabase.getInstance().getReference("Withdraw-Requests")
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid

        currentUser?.let { user ->
            val query = balanceRef.orderByChild("withdrawId").equalTo(id)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val withdrawData = snapshot.getValue(withdrawReq::class.java)
                            if (withdrawData != null) {
                                // Update the existing balance data

                                withdrawData.userReqStatus = ApplicationStatus
                                withdrawData.WithdrawAmount = withdrawAmount

                                // Save the updated balance data
                                snapshot.ref.setValue(withdrawData)
                                    .addOnSuccessListener {
                                        // Balance data updated successfully
                                        // You can perform any additional actions here if needed
                                        Toast.makeText(context, "Request Updated Successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        // Failed to update balance data
                                        Toast.makeText(context, "Failed to update balance data: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    } else {
                        // Handle case where balance data for the user doesn't exist
                        Toast.makeText(context, "Balance data for the user does not exist", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    Toast.makeText(context, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }



    fun updateData(newData: List<withdrawReq>) {
        withdrawList.clear()
        withdrawList.addAll(newData)
        notifyDataSetChanged()
    }


    private fun removeWDReq(uid: String) {
        // Remove or mark the request as processed in the DepositRequests database
        val depositRequestsRef = FirebaseDatabase.getInstance().getReference("Withdraw-Requests")
        val query = depositRequestsRef.orderByChild("withdrawId").equalTo(uid)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        // Remove the deposit request node
                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                // Request removed successfully
                                //Toast.makeText(context, "Deposit request removed successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                // Failed to remove request
                                Toast.makeText(
                                    context,
                                    "Failed to remove deposit request: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    // Handle case where the deposit request doesn't exist
                    Toast.makeText(context, "Deposit request does not exist", Toast.LENGTH_SHORT)
                        .show()
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