package com.itdocx.oneforce.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneforce.Models.dataOfcomplains
import com.itdocx.oneforce.R

class UserComplaintsAdapter(
    val context: Context,
    val complaintList: MutableList<dataOfcomplains>
) : RecyclerView.Adapter<UserComplaintsAdapter.ViewHolder>() {


    private var filteredUsersList: MutableList<dataOfcomplains> = mutableListOf()

    init {
        filteredUsersList.addAll(complaintList)
    }

    // This function will update the adapter's dataset based on the filtered list
    fun filterList(filteredList: List<dataOfcomplains>) {
        filteredUsersList.clear()
        filteredUsersList.addAll(filteredList)
        notifyDataSetChanged()
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtName = itemView.findViewById<TextView>(R.id.txtUCName)
        val txtEmail = itemView.findViewById<TextView>(R.id.txtUCemail)
        val txtComplain = itemView.findViewById<TextView>(R.id.txtAdminUC)
        val ivClose = itemView.findViewById<ImageView>(R.id.ivUCclose)
        fun bind(user: dataOfcomplains) {
            txtName.text = user.name
            txtEmail.text = user.email
            txtComplain.text = user.issue
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserComplaintsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.complains_list_items, parent, false)


        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserComplaintsAdapter.ViewHolder, position: Int) {

       val itemComplains = complaintList[position] // Use filtered list here
        val itemFilter = filteredUsersList[position]

        holder.txtName.text = itemComplains.name
        holder.txtEmail.text = itemComplains.email
        holder.txtComplain.text = itemComplains.issue

        holder.ivClose.setOnClickListener {
            removeuserComplain(itemComplains.complainID)
        }
        holder.bind(itemFilter)

    }

    override fun getItemCount(): Int {
        //return complaintList.size
        return filteredUsersList.size
    }


    private fun removeuserComplain(uid: String) {
        val depositRequestsRef =
            FirebaseDatabase.getInstance().getReference("User-Complains")
        val query = depositRequestsRef.orderByChild("complainID").equalTo(uid)

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
                                    "Complain Removed successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Failed to remove user complain: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "User complain does not exist",
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