package com.itdocx.oneforce.WalletUtils

class WalletBalance {
    var balance: Double = 0.0
    var transactionHistory: MutableList<Transaction> = mutableListOf()

    fun deposit(amount: Double) {
        balance += amount
        transactionHistory.add(Transaction(amount, "Deposit", System.currentTimeMillis()))
    }

    fun withdraw(amount: Double) {
        if (amount <= balance) {
            balance -= amount
            transactionHistory.add(Transaction(amount, "Withdrawal", System.currentTimeMillis()))
        } else {
            // Handle insufficient funds
            // You may want to throw an exception or return an error code

        }
    }
}

data class Transaction(val amount: Double, val type: String, val timestamp: Long)
