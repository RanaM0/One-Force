package com.itdocx.oneapp.Models

data class UserRequests(
    val imageUri: String = "", // URI of the image
    val userName: String = "", // Current user's name
    val email:String = "",
    val requestId: String = "", // Current user's ID
    val currenBalance: String ="",
    val uid:String = ""
)
{
    constructor() : this("", "", "", "", "","")
}