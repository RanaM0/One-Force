package com.itdocx.oneapp.Models

data class investmentData(
    var name:String,
    var email:String,
    var investment:String,
    var cycle:String,
    var profit:String,
    var investmentId: String,
    var userid:String
)
{
    constructor() : this("", "", "","","0.00","","")
}
