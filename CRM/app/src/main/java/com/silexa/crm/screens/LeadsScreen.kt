package com.silexa.crm.screens

import androidx.compose.foundation.background
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.silexa.crm.call.CallManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silexa.crm.data.Channel
import com.silexa.crm.data.Lead
import com.silexa.crm.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.UUID

@Composable
fun LeadsScreen() {

    // ==================================================
    // STATE
    // ==================================================

    var channels by remember {
        mutableStateOf<List<Channel>>(emptyList())
    }

    var selectedChannel by remember {
        mutableStateOf("All")
    }

    var leads by remember {
        mutableStateOf<List<Lead>>(emptyList())
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var isLoadingChannels by remember {
        mutableStateOf(true)
    }

    var isLoadingLeads by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var showManualLeadDialog by remember {
        mutableStateOf(false)
    }

    var refreshKey by remember {
        mutableStateOf(0)
    }


    // ==================================================
    // GET CHANNELS
    // ==================================================

    LaunchedEffect(Unit) {

        try {

            isLoadingChannels = true
            errorMessage = null

            val response =
                RetrofitClient
                    .apiService
                    .getChannels()

            // Manual is intentionally NOT here.
            //
            // Manual is always added separately below.

            channels =
                response.filter {
                    it.connection_status.equals(
                        "connected",
                        ignoreCase = true
                    )
                }

        } catch (e: Exception) {

            errorMessage =
                e.message ?: "Unable to load channels"

        } finally {

            isLoadingChannels = false
        }
    }


    // ==================================================
    // GET LEADS
    // ==================================================

    LaunchedEffect(
        selectedChannel,
        channels,
        isLoadingChannels,
        refreshKey
    ) {

        if (isLoadingChannels) {
            return@LaunchedEffect
        }

        try {

            isLoadingLeads = true
            errorMessage = null


            // ==========================================
            // ALL
            // ==========================================

            if (selectedChannel == "All") {

                val requests =
                    channels.map { channel ->

                        async {

                            try {

                                val uuid =
                                    UUID.fromString(
                                        channel.id
                                    )

                                RetrofitClient
                                    .apiService
                                    .getLeads(
                                        channelId = uuid,
                                        page = 1,
                                        pageSize = 20,
                                        sortBy =
                                            "created_at",
                                        sortOrder =
                                            "desc"
                                    )
                                    .Leaddetails

                            } catch (
                                e: Exception
                            ) {

                                emptyList<Lead>()
                            }
                        }
                    }


                val channelLeads =
                    requests
                        .awaitAll()
                        .flatten()


                // Manual leads
                //
                // Manual = channel_id NULL

                val manualLeads =
                    try {

                        RetrofitClient
                            .apiService
                            .getLeads(
                                channelId = null,
                                page = 1,
                                pageSize = 20,
                                sortBy =
                                    "created_at",
                                sortOrder =
                                    "desc"
                            )
                            .Leaddetails

                    } catch (
                        e: Exception
                    ) {

                        emptyList()
                    }


                leads =
                    (
                            channelLeads +
                                    manualLeads
                            )
                        .sortedByDescending {
                            it.created_at
                        }
            }


            // ==========================================
            // MANUAL
            // ==========================================

            else if (
                selectedChannel == "Manual"
            ) {

                val response =
                    RetrofitClient
                        .apiService
                        .getLeads(
                            channelId = null,
                            page = 1,
                            pageSize = 20,
                            sortBy =
                                "created_at",
                            sortOrder =
                                "desc"
                        )

                leads =
                    response.Leaddetails
            }


            // ==========================================
            // SPECIFIC CHANNEL
            // ==========================================

            else {

                val selected =
                    channels.find {
                        it.name ==
                                selectedChannel
                    }

                if (selected != null) {

                    val uuid =
                        UUID.fromString(
                            selected.id
                        )

                    val response =
                        RetrofitClient
                            .apiService
                            .getLeads(
                                channelId = uuid,
                                page = 1,
                                pageSize = 20,
                                sortBy =
                                    "created_at",
                                sortOrder =
                                    "desc"
                            )

                    leads =
                        response.Leaddetails
                }
            }

        } catch (e: Exception) {

            errorMessage =
                e.message ?: "Unable to load leads"

        } finally {

            isLoadingLeads = false
        }
    }


    // ==================================================
    // SEARCH
    // ==================================================

    val filteredLeads =
        remember(
            leads,
            searchQuery
        ) {

            if (
                searchQuery
                    .isBlank()
            ) {

                leads

            } else {

                val query =
                    searchQuery
                        .trim()
                        .lowercase()

                leads.filter { lead ->

                    lead.name
                        .lowercase()
                        .contains(query) ||

                            lead.email
                                .lowercase()
                                .contains(query) ||

                            (
                                    lead.phone_number
                                        ?: ""
                                    )
                                .lowercase()
                                .contains(query)
                }
            }
        }


    // ==================================================
    // CHANNEL FILTERS
    //
    // API channels + Manual
    // ==================================================

    val filterChannels =
        remember(channels) {

            listOf("All") +
                    channels.map {
                        it.name
                    } +
                    listOf("Manual")
        }


    // ==================================================
    // SCREEN
    // ==================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme
                    .colorScheme
                    .background
            )
    ) {

        // ==================================================
        // MAIN CONTENT
        // ==================================================

        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
        ) {

            // ----------------------------------------------
            // HEADER
            // ----------------------------------------------

            item {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 24.dp,
                            bottom = 16.dp
                        )
                ) {

                    Text(
                        text = "Leads",
                        fontSize = 28.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "Channel-wise incoming customer leads",
                        fontSize = 14.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }


            // ----------------------------------------------
            // SEARCH
            // ----------------------------------------------

            item {

                OutlinedTextField(
                    value =
                        searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 20.dp
                            ),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "Search leads by name, email or phone..."
                        )
                    },
                    shape =
                        RoundedCornerShape(
                            10.dp
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )
            }


            // ----------------------------------------------
            // STICKY FILTERS
            // ----------------------------------------------

            stickyHeader {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme
                                .colorScheme
                                .background
                        )
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 4.dp,
                            bottom = 10.dp
                        )
                ) {

                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                8.dp
                            )
                    ) {

                        items(
                            items =
                                filterChannels,
                            key = {
                                it
                            }
                        ) { channelName ->

                            ChannelFilter(
                                name =
                                    channelName,
                                selected =
                                    selectedChannel ==
                                            channelName,
                                onClick = {

                                    selectedChannel =
                                        channelName
                                }
                            )
                        }
                    }
                }
            }


            // ----------------------------------------------
            // LOADING
            // ----------------------------------------------

            if (isLoadingLeads) {

                item {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    400.dp
                                ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    32.dp
                                )
                        )
                    }
                }
            }


            // ----------------------------------------------
            // ERROR
            // ----------------------------------------------

            else if (
                errorMessage != null
            ) {

                item {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    300.dp
                                ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text =
                                    errorMessage
                                        ?: "Something went wrong",
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .error
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        10.dp
                                    )
                            )

                            Text(
                                text =
                                    "Tap the channel again to retry",
                                fontSize = 12.sp,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                }
            }


            // ----------------------------------------------
            // EMPTY
            // ----------------------------------------------

            else if (
                filteredLeads.isEmpty()
            ) {

                item {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    300.dp
                                ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text =
                                    "No leads found",
                                fontSize = 16.sp,
                                fontWeight =
                                    FontWeight.Medium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        5.dp
                                    )
                            )

                            Text(
                                text =
                                    if (
                                        selectedChannel ==
                                        "Manual"
                                    ) {
                                        "No manual leads yet"
                                    } else {
                                        "No leads available for this channel"
                                    },
                                fontSize = 13.sp,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                }
            }


            // ----------------------------------------------
            // LEADS
            // ----------------------------------------------

            else {

                items(
                    items =
                        filteredLeads,
                    key = {
                        it.id
                    }
                ) { lead ->

                    LeadCard(
                        lead = lead
                    )
                }
            }


            item {

                Spacer(
                    modifier =
                        Modifier.height(
                            90.dp
                        )
                )
            }
        }


        // ==================================================
        // FLOATING ADD BUTTON
        // ==================================================

        FloatingActionButton(
            onClick = {
                showManualLeadDialog = true
            },
            modifier =
                Modifier
                    .align(
                        Alignment.BottomEnd
                    )
                    .padding(
                        end = 20.dp,
                        bottom = 20.dp
                    ),
            shape =
                RoundedCornerShape(
                    16.dp
                ),
            containerColor =
                MaterialTheme
                    .colorScheme
                    .primary
        ) {

            Text(
                text = "+",
                fontSize = 28.sp,
                fontWeight =
                    FontWeight.Light,
                color =
                    MaterialTheme
                        .colorScheme
                        .onPrimary
            )
        }


        // ==================================================
        // MANUAL LEAD DIALOG
        // ==================================================

        if (showManualLeadDialog) {

            ManualLeadDialog(
                onDismiss = {

                    showManualLeadDialog =
                        false
                },
                onLeadAdded = {

                    // Refresh the list
                    refreshKey++
                }
            )
        }
    }
}


// ======================================================
// CHANNEL FILTER
// ======================================================

@Composable
private fun ChannelFilter(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    9.dp
                )
            )
            .background(
                if (selected) {

                    MaterialTheme
                        .colorScheme
                        .primary

                } else {

                    MaterialTheme
                        .colorScheme
                        .surface
                }
            )
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 18.dp,
                vertical = 10.dp
            )
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight =
                    FontWeight.Medium,
                color =
                    if (selected) {

                        MaterialTheme
                            .colorScheme
                            .onPrimary

                    } else {

                        MaterialTheme
                            .colorScheme
                            .onSurface
                    }
            )

            // Small status dot for API channels
            if (
                name != "All" &&
                name != "Manual"
            ) {

                Spacer(
                    modifier =
                        Modifier.width(7.dp)
                )

                Box(
                    modifier =
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .primary
                            )
                )
            }
        }
    }
}


// ======================================================
// LEAD CARD
// ======================================================

@Composable
private fun LeadCard(
    lead: Lead
) {
    val context = LocalContext.current

    val callManager = remember {
        CallManager(context)
    }

    var pendingPhone by remember {
        mutableStateOf<String?>(null)
    }

    var pendingLeadId by remember {
        mutableStateOf<String?>(null)
    }

    var pendingLeadName by remember {
        mutableStateOf<String?>(null)
    }

    val callPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                val phone = pendingPhone

                if (!phone.isNullOrBlank()) {

                    callManager.startCall(
                        leadId = pendingLeadId ?: "",
                        leadName = pendingLeadName ?: "",
                        phoneNumber = phone
                    )
                }
            }
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 5.dp
                )
                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surface
                )
                .padding(
                    horizontal = 17.dp,
                    vertical = 15.dp
                )
    ) {

        // ----------------------------------------------
        // NAME + CHANNEL
        // ----------------------------------------------

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    lead.name.ifBlank {
                        "Unknown"
                    },
                fontSize = 16.sp,
                fontWeight =
                    FontWeight.SemiBold,
                modifier =
                    Modifier.weight(1f)
            )

            // Lead source indicator
            Box(
                modifier =
                    Modifier
                        .clip(
                            RoundedCornerShape(
                                6.dp
                            )
                        )
                        .background(
                            MaterialTheme
                                .colorScheme
                                .primary
                                .copy(
                                    alpha = 0.10f
                                )
                        )
                        .padding(
                            horizontal = 9.dp,
                            vertical = 4.dp
                        )
            ) {

                Text(
                    text =
                        if (lead.connection_id?.isBlank() != false) {
                            "Manual"
                        } else {
                            "Lead"
                        },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(9.dp)
        )


        // ----------------------------------------------
        // EMAIL
        // ----------------------------------------------

        if (
            lead.email.isNotBlank()
        ) {

            Text(
                text =
                    lead.email,
                fontSize = 14.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }


        // ----------------------------------------------
        // PHONE
        // ----------------------------------------------

        if (!lead.phone_number.isNullOrBlank()) {

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = lead.phone_number!!,
                    fontSize = 14.sp,
                    color = MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                )

                Text(
                    text = "📞",
                    fontSize = 22.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {

                            val phone = lead.phone_number

                            if (!phone.isNullOrBlank()) {

                                pendingPhone = phone
                                pendingLeadId = lead.id
                                pendingLeadName = lead.name

                                if (
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CALL_PHONE
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {

                                    callManager.startCall(
                                        leadId = lead.id,
                                        leadName = lead.name,
                                        phoneNumber = phone
                                    )

                                } else {

                                    callPermissionLauncher.launch(
                                        Manifest.permission.CALL_PHONE
                                    )
                                }
                            }
                        }
                        .padding(8.dp)
                )
            }
        }


        // ----------------------------------------------
        // DATE
        // ----------------------------------------------

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        Text(
            text =
                formatLeadDate(
                    lead.created_at
                ),
            fontSize = 11.sp,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}


// ======================================================
// DATE FORMATTER
// ======================================================

private fun formatLeadDate(value: String): String {

    return try {

        val inputFormat =
            java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                java.util.Locale.getDefault()
            )

        inputFormat.timeZone =
            java.util.TimeZone.getTimeZone("UTC")

        val date =
            inputFormat.parse(value)

        val outputFormat =
            java.text.SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                java.util.Locale.getDefault()
            )

        outputFormat.format(date)

    } catch (e: Exception) {

        value
    }
}