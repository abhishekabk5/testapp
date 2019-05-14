package com.arlak.testapp.visitorFragment

import android.net.Uri
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Visitor(
    val phoneNumber: String?,
    val photoDownloadUrl: Uri?,
    val visitCount: Int = 1
) {
    constructor() : this(null, null)
}
