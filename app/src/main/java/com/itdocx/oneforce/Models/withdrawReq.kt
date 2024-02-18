package com.itdocx.oneforce.Models

data class withdrawReq(

   // URI of the image
    val usrName: String = "", // Current user's name
    val emale:String = "",
    val withdrawId: String = "", // Current user's ID
    val currentBalance: String ="",
    var WithdrawAmount: String? =null,
    val accountNumber: String = "",
    val bankName: String = "",
    var userReqStatus: String = "",
    val uid:String = ""
)
{
    constructor() : this("", "", "", "", "","","","","")
}
