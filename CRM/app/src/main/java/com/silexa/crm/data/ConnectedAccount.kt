package com.silexa.crm.data

data class ConnectedAccount(
    val id: String,
    val provider_identifier: String,
    val messagesubscription: Boolean
)