package com.itdocx.oneforce.Models

data class dataOfcomplains(

    val name:String,
    val email:String,
    val issue:String,
    val complainID:String,
    val UserId:String
){
    // No-argument constructor is either explicitly defined or automatically generated by Kotlin

    constructor() : this("", "", "", "","")

}
