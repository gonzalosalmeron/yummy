package com.gonzxlodev.yummy.model

import java.util.*

data class Recipe (
    var id: String? = null,
    var name: String? = null,
    var ingredients: String? = null,
    var diners: String? = null,
    var preparation_time: String? = null,
    var description: String? = null,
    var tag: String? = null,
    var imgUrl: String? = null,
    var user_email: String? = null,
    var user_name: String? = null,
    var user_imgUrl: String? = null,
    var created_at: Date? = null
)