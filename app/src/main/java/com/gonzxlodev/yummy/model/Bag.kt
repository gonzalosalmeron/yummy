package com.gonzxlodev.yummy.model

import java.util.*

data class Bag (
    var id: String? = null,
    var name: String? = null,
    var completed: Boolean? = null,
    var user_email: String? = null,
    var created_at: Date? = null,
)