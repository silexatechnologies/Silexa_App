package com.silexa.crm.call

import com.silexa.crm.call.recording.CallRecordingReader
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import java.util.concurrent.Executors

class CrmPhoneStateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CRM_CALL"

        /*
         * Call Log can take a short amount of time to become
         * available after the phone reports IDLE.
         */
        private val RETRY_DELAYS = longArrayOf(
            500L,
            1500L,
            3000L
        )
    }

    private fun searchForRecording(
        context: Context,
        callDateMillis: Long,
        callDurationSeconds: Long,
        attempt: Int
    ) {
        executor.execute {
            try {
                val delays = longArrayOf(
                    1000L,
                    3000L,
                    5000L,
                    8000L
                )

                if (attempt > 0) {
                    Thread.sleep(delays[attempt - 1])
                }

                Log.d(TAG, "================================")
                Log.d(TAG, "SEARCHING FOR CALL RECORDING")
                Log.d(TAG, "Attempt: ${attempt + 1}")
                Log.d(TAG, "Call date: $callDateMillis")
                Log.d(TAG, "Call duration: $callDurationSeconds seconds")
                Log.d(TAG, "================================")

                val recordingReader =
                    CallRecordingReader(context)

                val recording =
                    recordingReader.findRecording(
                        callDateMillis = callDateMillis,
                        callDurationSeconds = callDurationSeconds
                    )

                if (recording != null) {

                    Log.d(TAG, "================================")
                    Log.d(TAG, "🎙️ CALL RECORDING FOUND")
                    Log.d(TAG, "Name: ${recording.displayName}")
                    Log.d(TAG, "URI: ${recording.uri}")
                    Log.d(TAG, "Duration: ${recording.duration} ms")
                    Log.d(TAG, "Size: ${recording.size} bytes")
                    Log.d(TAG, "================================")

                } else {

                    Log.d(TAG, "NO CALL RECORDING FOUND")

                    if (attempt < delays.lastIndex) {

                        Log.d(
                            TAG,
                            "RETRYING RECORDING SEARCH"
                        )

                        searchForRecording(
                            context = context,
                            callDateMillis = callDateMillis,
                            callDurationSeconds = callDurationSeconds,
                            attempt = attempt + 1
                        )

                    } else {

                        Log.d(
                            TAG,
                            "❌ CALL RECORDING NOT FOUND"
                        )
                    }
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "ERROR SEARCHING FOR CALL RECORDING",
                    e
                )
            }
        }
    }

    private val executor = Executors.newSingleThreadExecutor()

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        Log.d(
            TAG,
            "🔥 CrmPhoneStateReceiver RECEIVED: action=${intent.action}"
        )

        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            return
        }

        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            return
        }

        val state =
            intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        Log.d(
            TAG,
            "PHONE STATE RECEIVED: $state"
        )

        when (state) {

            TelephonyManager.EXTRA_STATE_RINGING -> {

                Log.d(
                    TAG,
                    "PHONE STATE = RINGING"
                )
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {

                Log.d(
                    TAG,
                    "PHONE STATE = OFFHOOK"
                )

                if (CallSession.hasPendingCall()) {

                    CallSession.sawOffhook = true

                    Log.d(
                        TAG,
                        "CRM CALL OFFHOOK DETECTED"
                    )

                    Log.d(
                        TAG,
                        "Attempt ID: ${CallSession.attemptId}"
                    )

                    Log.d(
                        TAG,
                        "Lead: ${CallSession.leadName}"
                    )

                    Log.d(
                        TAG,
                        "Phone: ${CallSession.phoneNumber}"
                    )
                }
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {

                Log.d(
                    TAG,
                    "PHONE STATE = IDLE"
                )

                handleIdle(context)
            }
        }
    }

    private fun handleIdle(
        context: Context
    ) {

        if (!CallSession.hasPendingCall()) {

            Log.d(
                TAG,
                "IDLE - NO PENDING CRM CALL"
            )

            return
        }

        /*
         * If OFFHOOK was never seen, the CRM call probably
         * never actually became active.
         *
         * Example:
         *
         * CRM call
         *     ↓
         * SIM selection
         *     ↓
         * User cancels
         *
         * There should be no CRM call record.
         */
        if (!CallSession.sawOffhook) {

            Log.d(
                TAG,
                "IDLE BUT CRM CALL NEVER REACHED OFFHOOK"
            )

            Log.d(
                TAG,
                "LIKELY CALL CANCELLATION"
            )

            CallSession.clear()

            return
        }

        /*
         * Prevent duplicate IDLE broadcasts from starting
         * multiple Call Log searches.
         */
        if (CallSession.processingCallLog) {

            Log.d(
                TAG,
                "CALL LOG PROCESSING ALREADY RUNNING"
            )

            return
        }

        CallSession.processingCallLog = true

        Log.d(
            TAG,
            "CRM CALL DISCONNECTED"
        )

        Log.d(
            TAG,
            "STARTING CALL LOG SEARCH"
        )

        searchCallLogAfterDisconnect(
            context = context,
            attempt = 0
        )
    }

    private fun searchCallLogAfterDisconnect(
        context: Context,
        attempt: Int
    ) {

        val pendingAttemptId =
            CallSession.attemptId

        val leadId =
            CallSession.leadId

        val leadName =
            CallSession.leadName

        val phoneNumber =
            CallSession.phoneNumber

        val requestTime =
            CallSession.callRequestTime

        if (
            pendingAttemptId == null ||
            leadId == null ||
            phoneNumber == null ||
            requestTime == null
        ) {

            Log.d(
                TAG,
                "CALL SESSION DATA MISSING"
            )

            CallSession.clear()
            return
        }

        executor.execute {

            try {

                val reader =
                    CallLogReader(context)

                Log.d(
                    TAG,
                    "================================"
                )

                Log.d(
                    TAG,
                    "POST-DISCONNECT CALL LOG SEARCH"
                )

                Log.d(
                    TAG,
                    "Attempt: ${attempt + 1}"
                )

                Log.d(
                    TAG,
                    "Attempt ID: $pendingAttemptId"
                )

                Log.d(
                    TAG,
                    "Lead ID: $leadId"
                )

                Log.d(
                    TAG,
                    "Lead Name: $leadName"
                )

                Log.d(
                    TAG,
                    "Phone: $phoneNumber"
                )

                Log.d(
                    TAG,
                    "Request Time: $requestTime"
                )

                Log.d(
                    TAG,
                    "================================"
                )

                val call =
                    reader.getLatestOutgoingCall(
                        phoneNumber = phoneNumber,
                        minimumDate = requestTime
                    )

                if (call != null) {

                    Log.d(
                        TAG,
                        "================================"
                    )

                    Log.d(
                        TAG,
                        "CRM CALL LOG MATCHED"
                    )

                    Log.d(
                        TAG,
                        "Attempt ID: $pendingAttemptId"
                    )

                    Log.d(
                        TAG,
                        "Lead ID: $leadId"
                    )

                    Log.d(
                        TAG,
                        "Lead Name: $leadName"
                    )

                    Log.d(
                        TAG,
                        "Phone: ${call.phoneNumber}"
                    )

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
                        "================================"
                    )

                    /*
                     * This is currently where we only LOG.
                     *
                     * Later this exact block will become:
                     *
                     * API request → backend
                     *
                     * using pendingAttemptId as the unique
                     * CRM call record ID.
                     */
                    reader.logCall(
                        call = call,
                        leadId = leadId,
                        leadName = leadName
                    )

                    //Search for Recording
                    searchForRecording(
                        context = context,
                        callDateMillis = call.date,
                        callDurationSeconds = call.duration,
                        attempt = 0
                    )

                    /*
                     * Successful match.
                     *
                     * Now it is safe to clear the attempt.
                     */
                    CallSession.clear()

                } else {

                    Log.d(
                        TAG,
                        "NO MATCHING CALL LOG YET"
                    )

                    if (attempt < RETRY_DELAYS.lastIndex) {

                        val delay =
                            RETRY_DELAYS[attempt]

                        Log.d(
                            TAG,
                            "RETRYING AFTER ${delay}ms"
                        )

                        Thread.sleep(delay)

                        searchCallLogAfterDisconnect(
                            context = context,
                            attempt = attempt + 1
                        )

                    } else {

                        /*
                         * We reached the end of the
                         * post-disconnect retry window.
                         */
                        Log.d(
                            TAG,
                            "NO MATCHING CALL LOG FOUND"
                        )

                        Log.d(
                            TAG,
                            "CALL ATTEMPT WILL BE CLEARED"
                        )

                        CallSession.clear()
                    }
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "ERROR WHILE PROCESSING CALL LOG",
                    e
                )

                CallSession.clear()
            }
        }
    }
}