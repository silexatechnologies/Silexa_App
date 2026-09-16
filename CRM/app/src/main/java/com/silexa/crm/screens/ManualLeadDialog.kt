package com.silexa.crm.screens

import androidx.compose.foundation.layout.size
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silexa.crm.network.ManualLeadRequest
import com.silexa.crm.network.RetrofitClient
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun ManualLeadDialog(
    onDismiss: () -> Unit,
    onLeadAdded: () -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var phone by remember {
        mutableStateOf("")
    }

    var isSubmitting by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var showSuccess by remember {
        mutableStateOf(false)
    }

    var visible by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // ==================================================
        // BACKGROUND
        // ==================================================

        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150))
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.32f)
                    )
                    .clickable(
                        indication = null,
                        interactionSource =
                            remember {
                                MutableInteractionSource()
                            }
                    ) {

                        if (
                            !isSubmitting &&
                            !showSuccess
                        ) {
                            onDismiss()
                        }
                    }
            )
        }


        // ==================================================
        // ADD LEAD SHEET
        // ==================================================

        if (!showSuccess) {

            AnimatedVisibility(
                visible = visible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                enter =
                    fadeIn(tween(180)) +
                            slideInVertically(
                                animationSpec =
                                    tween(
                                        350,
                                        easing =
                                            FastOutSlowInEasing
                                    ),
                                initialOffsetY = {
                                    it
                                }
                            ) +
                            scaleIn(
                                animationSpec =
                                    tween(
                                        350,
                                        easing =
                                            FastOutSlowInEasing
                                    ),
                                initialScale = 0.94f
                            )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                topStart = 28.dp,
                                topEnd = 28.dp
                            )
                        )
                        .background(
                            MaterialTheme
                                .colorScheme
                                .surface
                        )
                        .padding(
                            start = 22.dp,
                            end = 22.dp,
                            top = 12.dp,
                            bottom = 28.dp
                        )
                ) {

                    // Handle

                    Box(
                        modifier = Modifier
                            .align(
                                Alignment.CenterHorizontally
                            )
                            .padding(
                                bottom = 18.dp
                            )
                            .height(4.dp)
                            .fillMaxWidth(0.12f)
                            .clip(
                                RoundedCornerShape(50)
                            )
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .outlineVariant
                            )
                    )


                    // Header

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    "Add Manual Lead",
                                fontSize = 22.sp,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(
                                text =
                                    "Add a lead directly to your CRM",
                                fontSize = 13.sp,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                            )
                        }

                        Text(
                            text = "×",
                            fontSize = 28.sp,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,
                            modifier = Modifier
                                .clickable {

                                    if (
                                        !isSubmitting
                                    ) {
                                        onDismiss()
                                    }
                                }
                                .padding(8.dp)
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(22.dp)
                    )


                    // Name

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = null
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text("Name")
                        },
                        placeholder = {
                            Text(
                                "Enter lead name"
                            )
                        },
                        singleLine = true,
                        shape =
                            RoundedCornerShape(12.dp)
                    )


                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )


                    // Email

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text("Email")
                        },
                        placeholder = {
                            Text(
                                "Enter email address"
                            )
                        },
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Email
                            ),
                        shape =
                            RoundedCornerShape(12.dp)
                    )


                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )


                    // Phone

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            errorMessage = null
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text("Phone")
                        },
                        placeholder = {
                            Text(
                                "Enter phone number"
                            )
                        },
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Phone
                            ),
                        shape =
                            RoundedCornerShape(12.dp)
                    )


                    // Error

                    if (
                        errorMessage != null
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Text(
                            text =
                                errorMessage!!,
                            fontSize = 13.sp,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(22.dp)
                    )


                    // Add button

                    Button(
                        onClick = {

                            when {

                                name.isBlank() -> {

                                    errorMessage =
                                        "Please enter the lead name"
                                }

                                email.isBlank() -> {

                                    errorMessage =
                                        "Please enter the email"
                                }

                                phone.isBlank() -> {

                                    errorMessage =
                                        "Please enter the phone number"
                                }

                                else -> {

                                    isSubmitting =
                                        true

                                    errorMessage =
                                        null

                                    scope.launch {

                                        try {

                                            val request =
                                                ManualLeadRequest(
                                                    name =
                                                        name.trim(),
                                                    email =
                                                        email.trim(),
                                                    phone_number =
                                                        phone.trim()
                                                )

                                            val response =
                                                RetrofitClient
                                                    .apiService
                                                    .createManualLead(
                                                        request
                                                    )

                                            if (
                                                response
                                                    .isSuccessful
                                            ) {

                                                isSubmitting =
                                                    false

                                                showSuccess =
                                                    true

                                                onLeadAdded()

                                            } else {

                                                isSubmitting =
                                                    false

                                                errorMessage =
                                                    "Unable to add lead. Please try again."
                                            }

                                        } catch (
                                            e: Exception
                                        ) {

                                            isSubmitting =
                                                false

                                            errorMessage =
                                                e.message
                                                    ?: "Something went wrong"
                                        }
                                    }
                                }
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        enabled =
                            !isSubmitting,
                        shape =
                            RoundedCornerShape(12.dp)
                    ) {

                        if (isSubmitting) {

                            CircularProgressIndicator(
                                modifier =
                                    Modifier.height(
                                        22.dp
                                    ),
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimary,
                                strokeWidth = 2.dp
                            )

                        } else {

                            Text(
                                text = "Add Lead",
                                fontSize = 16.sp,
                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }


        // ==================================================
        // SUCCESS POPUP
        // ==================================================

        if (showSuccess) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha = 0.25f
                        )
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                AnimatedVisibility(
                    visible = true,
                    enter =
                        fadeIn(tween(200)) +
                                scaleIn(
                                    animationSpec =
                                        tween(300),
                                    initialScale =
                                        0.82f
                                )
                ) {

                    Column(
                        modifier = Modifier
                            .padding(28.dp)
                            .clip(
                                RoundedCornerShape(
                                    22.dp
                                )
                            )
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .surface
                            )
                            .padding(
                                horizontal = 28.dp,
                                vertical = 30.dp
                            ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                                ),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text = "✓",
                                fontSize = 28.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimary
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Text(
                            text =
                                "Lead Added Successfully",
                            fontSize = 19.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(7.dp)
                        )

                        Text(
                            text =
                                "The lead has been added to your CRM.",
                            fontSize = 13.sp,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )

                        Spacer(
                            modifier =
                                Modifier.height(22.dp)
                        )

                        Button(
                            onClick = {
                                onDismiss()
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                            shape =
                                RoundedCornerShape(
                                    11.dp
                                )
                        ) {

                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}