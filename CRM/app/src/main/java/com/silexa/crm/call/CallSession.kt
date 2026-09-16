package com.silexa.crm.call

import java.util.UUID

object CallSession {

    // Unique ID for this particular CRM call attempt.
    // This will later become the backend call record ID.
    var attemptId: String? = null

    // CRM lead information.
    var leadId: String? = null
    var leadName: String? = null
    var phoneNumber: String? = null

    // When CRM requested the native phone call.
    var callRequestTime: Long? = null

    /*
     * Becomes true when PHONE_STATE reports OFFHOOK.
     *
     * This helps distinguish:
     *
     * CRM call → SIM selection → Cancel
     *
     * from:
     *
     * CRM call → actually started → eventually ended
     */
    var sawOffhook = false

    /*
     * Prevents duplicate IDLE broadcasts from starting
     * multiple CallLog searches at the same time.
     */
    var processingCallLog = false

    fun hasPendingCall(): Boolean {
        return attemptId != null &&
                leadId != null &&
                phoneNumber != null &&
                callRequestTime != null
    }

    fun createCallAttempt(
        leadId: String,
        leadName: String,
        phoneNumber: String
    ) {

        /*
         * Generate a completely new ID for every call attempt.
         *
         * Same lead + same phone number can therefore have
         * multiple separate call records.
         */
        attemptId = UUID.randomUUID().toString()

        this.leadId = leadId
        this.leadName = leadName
        this.phoneNumber = phoneNumber

        callRequestTime = System.currentTimeMillis()

        sawOffhook = false
        processingCallLog = false

        println("================================")
        println("NEW CRM CALL ATTEMPT")
        println("Attempt ID: $attemptId")
        println("Lead ID: $leadId")
        println("Lead Name: $leadName")
        println("Phone: $phoneNumber")
        println("Call Request Time: $callRequestTime")
        println("================================")
    }

    fun clear() {

        println("CLEARING CRM CALL SESSION")

        println("Attempt ID: $attemptId")
        println("Lead ID: $leadId")
        println("Lead Name: $leadName")

        attemptId = null
        leadId = null
        leadName = null
        phoneNumber = null
        callRequestTime = null

        sawOffhook = false
        processingCallLog = false
    }
}