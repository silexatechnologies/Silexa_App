package com.silexa.crm.call

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openDialer(
    context: Context,
    phoneNumber: String
) {
    if (phoneNumber.isBlank()) {
        return
    }

    val intent = Intent(
        Intent.ACTION_DIAL,
        Uri.parse("tel:${Uri.encode(phoneNumber)}")
    )

    context.startActivity(intent)
}