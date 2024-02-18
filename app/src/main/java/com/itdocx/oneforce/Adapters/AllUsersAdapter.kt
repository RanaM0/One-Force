package com.itdocx.oneforce.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itdocx.oneforce.Models.dataofUser
import com.itdocx.oneforce.R

class AllUsersAdapter(
    private val context: Context,
    private val usersList: MutableList<dataofUser>
) : RecyclerView.Adapter<AllUsersAdapter.ViewHolder>() {

    private var filteredUsersList: MutableList<dataofUser> = mutableListOf()

    init {
        filteredUsersList.addAll(usersList)
    }

    // This function will update the adapter's dataset based on the filtered list
    fun filterList(filteredList: List<dataofUser>) {
        filteredUsersList.clear()
        filteredUsersList.addAll(filteredList)
        notifyDataSetChanged()
    }



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtname = itemView.findViewById<TextView>(R.id.txtadminusrName)
        val txtemail = itemView.findViewById<TextView>(R.id.txtadminUserEmail)
        val txtId = itemView.findViewById<TextView>(R.id.txtadminUserID)
        val txtphone = itemView.findViewById<TextView>(R.id.txtadminUserPhone)
        val txtBalance = itemView.findViewById<TextView>(R.id.txtadminUserBalance)


        fun bind(user: dataofUser) {
            txtname.text = user.name.toString()
            txtemail.text = user.email.toString()
            txtId.text = user.cnic.toString()
            txtphone.text = user.phoneNumber.toString()
            txtBalance.text = user.walletCurrentbalance.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllUsersAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.all_winners_list_item, parent, false)

        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: AllUsersAdapter.ViewHolder, position: Int) {
        val listUsers = usersList[position]
        val user = filteredUsersList[position]
        holder.txtname.text = listUsers.name.toString()
        holder.txtemail.text = listUsers.email.toString()
        holder.txtId.text = listUsers.cnic.toString()
        holder.txtphone.text = listUsers.phoneNumber.toString()
        holder.txtBalance.text = listUsers.walletCurrentbalance.toString()


        holder.bind(user)

    }

    override fun getItemCount(): Int {
     //   return usersList.size
        return filteredUsersList.size
    }

}