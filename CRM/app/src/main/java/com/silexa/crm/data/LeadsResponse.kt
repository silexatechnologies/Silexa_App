package com.silexa.crm.data

data class LeadsResponse(
    val channel_id: String,
    val Leaddetails: List<Lead>,
    val page: Int,
    val page_size: Int,
    val total: Int,
    val total_pages: Int
)