package com.arlak.testapp.visitorFragment

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Visitor(
    val phoneNumber: String?,
    val photoDownloadUrl: String?,
    val visitCount: Int = 1
) {
    constructor() : this(null, null)
}
