package com.silexa.crm.call

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat

class CallManager(
    private val context: Context
) {

    companion object {
        private const val TAG = "CRM_CALL"
    }

    fun startCall(
        leadId: String,
        leadName: String,
        phoneNumber: String
    ) {

        Log.d(TAG, "================================")
        Log.d(TAG, "CRM CALL REQUEST")
        Log.d(TAG, "Lead ID: $leadId")
        Log.d(TAG, "Lead Name: $leadName")
        Log.d(TAG, "Phone: $phoneNumber")
        Log.d(TAG, "================================")

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "CALL_PHONE permission is NOT granted")
            return
        }

        // Create pending CRM call attempt.
        CallSession.createCallAttempt(
            leadId = leadId,
            leadName = leadName,
            phoneNumber = phoneNumber
        )

        val intent = Intent(
            Intent.ACTION_CALL,
            Uri.parse("tel:$phoneNumber")
        )

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {

            context.startActivity(intent)

            Log.d(TAG, "Native phone app launched")

        } catch (e: Exception) {

            Log.e(TAG, "Unable to launch native phone app", e)

            CallSession.clear()
        }
    }
}