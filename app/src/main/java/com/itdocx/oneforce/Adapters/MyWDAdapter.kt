package com.itdocx.oneforce.Adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneforce.Models.withdrawReq
import com.itdocx.oneforce.R

class MyWDAdapter(val context: Context, val lisMyWDReq: MutableList<withdrawReq>) :
    RecyclerView.Adapter<MyWDAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtWDName = itemView.findViewById<TextView>(R.id.userWDName)
        val txtWDEmail = itemView.findViewById<TextView>(R.id.userWDEmail)
        val txtWDAccno = itemView.findViewById<TextView>(R.id.userWDAccountNo)
        val txtWDBank = itemView.findViewById<TextView>(R.id.userWDbankName)
        val txtWDStatus = itemView.findViewById<TextView>(R.id.userWDstatus)
        val txtWDAmount = itemView.findViewById<TextView>(R.id.txtWDAmountTitle)
        val txtWDCurrentBalANCE = itemView.findViewById<TextView>(R.id.userWDBalance)
        val btnWDApproveReq = itemView.findViewById<Button>(R.id.btnWDApprove)
        val btnWDRejectReq = itemView.findViewById<Button>(R.id.btnWDReject)
        val btnWDCloseReq = itemView.findViewById<Button>(R.id.btnWDReject)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.withdraw_list_items, parent, false)


        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        val wdItems = lisMyWDReq[position]


        holder.txtWDName.text = wdItems.usrName
        holder.txtWDEmail.text = wdItems.emale
        holder.txtWDAccno.text = wdItems.accountNumber
        holder.txtWDBank.text = wdItems.bankName
        holder.txtWDStatus.text = wdItems.userReqStatus
        holder.txtWDAmount.text = wdItems.WithdrawAmount
        holder.txtWDCurrentBalANCE.text = wdItems.currentBalance


        holder.btnWDCloseReq.visibility = View.GONE


        fetchWithdrawReqofUser(wdItems.uid,holder)


        holder.btnWDRejectReq.setText("Remove Request")
        val status = holder.txtWDStatus.text.toString()

        if (status.contains("Approved")){
            holder.txtWDStatus.setTextColor(Color.GREEN)

        }
        if (status.contains("Pending")){
            holder.txtWDStatus.setTextColor(Color.BLUE)

        }
        if (status.contains("Rejected")){
            holder.txtWDStatus.setTextColor(Color.RED)

        }
        holder.btnWDRejectReq.setOnClickListener {

            removeWithdrawReq(wdItems.withdrawId)
        }

        holder.btnWDApproveReq.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return lisMyWDReq.size
    }


    private fun removeWithdrawReq(uid: String) {
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

    private fun fetchWithdrawReqofUser(currentUserUid: String, holder: ViewHolder) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Withdraw-Requests")
        val query = databaseRef.orderByChild("uid").equalTo(currentUserUid)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(withdrawReq::class.java)
                    if (user?.uid == currentUserUid) {
                        holder.txtWDAmount.text = "$ "+user?.WithdrawAmount.toString()
                        // Optionally, you can break the loop if only one withdrawal request is expected
                        // break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Toast.makeText(
                    context,
                    "Failed to retrieve withdrawal requests",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


}