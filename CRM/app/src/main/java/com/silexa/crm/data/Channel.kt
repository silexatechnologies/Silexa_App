package com.silexa.crm.data

data class Channel(
    val id: String,
    val code: String,
    val name: String,
    val connection_status: String,
    val connected_accounts: List<ConnectedAccount>
)