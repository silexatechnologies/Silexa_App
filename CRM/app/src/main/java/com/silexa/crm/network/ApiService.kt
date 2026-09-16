package com.silexa.crm.network

import com.silexa.crm.data.Channel
import com.silexa.crm.data.LeadsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.UUID

data class ManualLeadRequest(
    val name: String,
    val email: String,
    val phone_number: String
)

interface ApiService {

    @GET("channels/all")
    suspend fun getChannels(): List<Channel>

    @GET("leads/all")
    suspend fun getLeads(
        @Query("channel_id") channelId: UUID?,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("sort_by") sortBy: String = "created_at",
        @Query("sort_order") sortOrder: String = "desc"
    ): LeadsResponse

    @POST("leads/create_manual_lead")
    suspend fun createManualLead(
        @Body request: ManualLeadRequest
    ): Response<Unit>
}