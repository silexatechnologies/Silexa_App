package com.silexa.crm.call

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import android.util.Log
import androidx.core.content.ContextCompat

data class CrmCallLogEntry(
    val phoneNumber: String?,
    val callType: Int,
    val date: Long,
    val duration: Long,
    val lastModified: Long
)

class CallLogReader(
    private val context: Context
) {

    companion object {
        private const val TAG = "CRM_CALL"
    }

    fun getLatestOutgoingCall(
        phoneNumber: String?,
        minimumDate: Long
    ): CrmCallLogEntry? {

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "READ_CALL_LOG permission is NOT granted")
            return null
        }

        /*
         * TEMPORARY DIAGNOSTIC:
         * Read many available CallLog fields so we can see whether
         * voicemail leaves any distinguishing information.
         */
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.LAST_MODIFIED,
            CallLog.Calls.PHONE_ACCOUNT_ID,
            CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME,
            CallLog.Calls.FEATURES,
            CallLog.Calls.VOICEMAIL_URI,
            CallLog.Calls.COUNTRY_ISO,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.CACHED_NUMBER_TYPE,
            CallLog.Calls.CACHED_NUMBER_LABEL
        )

        return try {

            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.DATE} >= ?",
                arrayOf(
                    CallLog.Calls.OUTGOING_TYPE.toString(),
                    minimumDate.toString()
                ),
                "${CallLog.Calls.DATE} DESC"
            )?.use { cursor ->

                while (cursor.moveToNext()) {

                    fun getStringColumn(
                        column: String
                    ): String? {
                        val index = cursor.getColumnIndex(column)
                        return if (
                            index >= 0 &&
                            !cursor.isNull(index)
                        ) {
                            cursor.getString(index)
                        } else {
                            null
                        }
                    }

                    fun getLongColumn(
                        column: String
                    ): Long? {
                        val index = cursor.getColumnIndex(column)
                        return if (
                            index >= 0 &&
                            !cursor.isNull(index)
                        ) {
                            cursor.getLong(index)
                        } else {
                            null
                        }
                    }

                    val number =
                        getStringColumn(CallLog.Calls.NUMBER)

                    val type =
                        getLongColumn(CallLog.Calls.TYPE)?.toInt() ?: -1

                    val date =
                        getLongColumn(CallLog.Calls.DATE) ?: 0L

                    val duration =
                        getLongColumn(CallLog.Calls.DURATION) ?: 0L

                    val lastModified =
                        getLongColumn(CallLog.Calls.LAST_MODIFIED) ?: 0L

                    /*
                     * Matching logic remains exactly the same.
                     */
                    if (
                        phoneNumber == null ||
                        numbersMatch(number, phoneNumber)
                    ) {

                        Log.d(TAG, "================================")
                        Log.d(TAG, "CALL LOG DIAGNOSTIC")
                        Log.d(TAG, "================================")

                        Log.d(TAG, "_ID: ${getStringColumn(CallLog.Calls._ID)}")

                        Log.d(TAG, "NUMBER: $number")

                        Log.d(TAG, "TYPE: $type")

                        Log.d(
                            TAG,
                            "DATE: ${CallUtils.formatTime(date)}"
                        )

                        Log.d(
                            TAG,
                            "DATE TIMESTAMP: $date"
                        )

                        Log.d(
                            TAG,
                            "DURATION: $duration seconds"
                        )

                        Log.d(
                            TAG,
                            "LAST_MODIFIED: $lastModified"
                        )

                        Log.d(
                            TAG,
                            "PHONE_ACCOUNT_ID: ${
                                getStringColumn(
                                    CallLog.Calls.PHONE_ACCOUNT_ID
                                )
                            }"
                        )

                        Log.d(
                            TAG,
                            "PHONE_ACCOUNT_COMPONENT_NAME: ${
                                getStringColumn(
                                    CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME
                                )
                            }"
                        )

                        Log.d(
                            TAG,
                            "FEATURES: ${
                                getLongColumn(
                                    CallLog.Calls.FEATURES
                                )
                            }"
                        )

                        Log.d(
                            TAG,
                            "VOICEMAIL_URI: ${
                                getStringColumn(
                                    CallLog.Calls.VOICEMAIL_URI
                                )
                            }"
                        )

                        Log.d(
                            TAG,
                            "COUNTRY_ISO: ${
                                getStringColumn(
                                    CallLog.Calls.COUNTRY_ISO
                                )
                            }"
                        )

                        Log.d(
                            TAG,
                            "CACHED_NAME: ${
                                getStringColumn(
                                    CallLog.Calls.CACHED_NAME
                                )
                            }"
                        )

                        Log.d(
                            TAG,
                            "CACHED_NUMBER_TYPE: ${
                                getLongColumn(
                                    CallLog.Calls.CACHED_NUMBER_TYPE
                                )
                            }"
                        )

                        Log.d(
                            TAG,
                            "CACHED_NUMBER_LABEL: ${
                                getStringColumn(
                                    CallLog.Calls.CACHED_NUMBER_LABEL
                                )
                            }"
                        )

                        Log.d(TAG, "================================")

                        return@use CrmCallLogEntry(
                            phoneNumber = number,
                            callType = type,
                            date = date,
                            duration = duration,
                            lastModified = lastModified
                        )
                    }
                }

                null
            }

        } catch (e: SecurityException) {

            Log.e(
                TAG,
                "SecurityException while reading Call Log",
                e
            )

            null

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Error while reading Call Log",
                e
            )

            null
        }
    }

    fun logCall(
        call: CrmCallLogEntry,
        leadId: String?,
        leadName: String?
    ) {

        Log.d(TAG, "================================")
        Log.d(TAG, "FINAL CRM CALL LOG")

        Log.d(TAG, "Lead ID: $leadId")
        Log.d(TAG, "Lead Name: $leadName")
        Log.d(TAG, "Phone: ${call.phoneNumber}")

        Log.d(TAG, "Type: OUTGOING")

        Log.d(
            TAG,
            "Call Date: ${CallUtils.formatTime(call.date)}"
        )

        Log.d(
            TAG,
            "Duration: ${call.duration} seconds"
        )

        Log.d(
            TAG,
            "Last Modified: ${call.lastModified}"
        )

        Log.d(
            TAG,
            "Date Timestamp: ${call.date}"
        )

        Log.d(
            TAG,
            "Duration Seconds: ${call.duration}"
        )

        Log.d(TAG, "================================")
    }

    private fun numbersMatch(
        first: String?,
        second: String?
    ): Boolean {

        if (
            first.isNullOrBlank() ||
            second.isNullOrBlank()
        ) {
            return false
        }

        val firstDigits =
            first.filter { it.isDigit() }

        val secondDigits =
            second.filter { it.isDigit() }

        if (firstDigits == secondDigits) {
            return true
        }

        if (
            firstDigits.length >= 10 &&
            secondDigits.length >= 10
        ) {
            return firstDigits.takeLast(10) ==
                    secondDigits.takeLast(10)
        }

        return false
    }
}