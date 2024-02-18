package com.itdocx.oneforce.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneforce.Models.UserRequests
import com.itdocx.oneforce.Models.dataofUser
import com.itdocx.oneforce.R

class RequestAdapter(
    private val context: Context,
    private val userList: MutableList<UserRequests>
) : RecyclerView.Adapter<RequestAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.DepositerName)
        val txtEmail: TextView = itemView.findViewById(R.id.depositerEmail)
        val txtreqId: TextView = itemView.findViewById(R.id.depositorNumber)
        val txtBalance: TextView = itemView.findViewById(R.id.depositorBalance)
        val imageProof: ImageView = itemView.findViewById(R.id.ivDepositProof)
        val etAmount: EditText = itemView.findViewById(R.id.etDepositFund)
        val btnDepFund: Button = itemView.findViewById(R.id.btnDeposiFunds)
        val btncancelFund: Button = itemView.findViewById(R.id.btnCancelFunds)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.deposit_req_list_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = userList[position]
        holder.txtName.text = request.userName
        holder.txtEmail.text = request.email
        holder.txtBalance.text = request.currenBalance
        Glide.with(holder.itemView).load(request.imageUri).into(holder.imageProof)
        val reqid = request.requestId
        if (reqid.contains(request.uid)) {
            holder.txtreqId.text = reqid.replace(Regex(request.uid), "")
        }


        holder.btnDepFund.setOnClickListener {

            val depositMoney = holder.etAmount.text.toString()
                .replace(Regex("[^0-9.]"), "") // Suppose depositMoney is "50"
            val currentBalance =
                holder.txtBalance.text.toString() // Suppose currentBalance is "$50.00"

// Remove non-numeric characters and dollar sign from currentBalance
            val cleanCurrentBalance = currentBalance.replace(Regex("[^0-9.]"), "")

// Convert the cleaned string to a Double
            val balance = cleanCurrentBalance.toDoubleOrNull()

// Now you can proceed similarly as before
            val depositAmount = depositMoney.toIntOrNull()

            if (depositAmount != null && balance != null) {
                val cumulativeMoney: Double = balance + depositAmount

                if (depositAmount >= 10) {
                    updateUserBalanceData(
                        request.userName,
                        request.email,
                        cumulativeMoney.toString(),
                        request.uid
                    )
                    holder.txtBalance.text =
                        cumulativeMoney.toString() // Update UI with new balance
                    removeRequest(request.requestId)
                } else {
                    Toast.makeText(
                        context,
                        "Deposit amount must be at least 10$",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Handle cases where conversion to integer fails
                Toast.makeText(
                    context,
                    "Invalid deposit amount or current balance",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.btncancelFund.setOnClickListener {
            removeRequest(request.requestId)
        }


    }

    override fun getItemCount(): Int {
        return userList.size
    }

    private fun updateUserBalanceData(
        name: String,
        email: String,
        currentBalance: String,
        id: String
    ) {
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
                                balanceData.name = name
                                balanceData.email = email
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


    private fun removeRequest(uid: String) {
        // Remove or mark the request as processed in the DepositRequests database
        val depositRequestsRef = FirebaseDatabase.getInstance().getReference("DepositRequests")
        val query = depositRequestsRef.orderByChild("requestId").equalTo(uid)

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