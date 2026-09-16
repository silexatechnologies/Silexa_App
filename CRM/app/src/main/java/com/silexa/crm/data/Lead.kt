package com.silexa.crm.data

data class Lead(
    val connection_id: String,
    val source_identifier: String,
    val id: String,
    val name: String,
    val email: String,
    val phone_number: String?,
    val created_at: String
)