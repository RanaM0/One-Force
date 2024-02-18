package com.itdocx.oneforce.Models

import java.io.Serializable

data class dataofAnouncements(

    var title: String,
    var anouncements: String
):Serializable {
    constructor() : this ("", "")
    }
