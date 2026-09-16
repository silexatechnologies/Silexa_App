package com.silexa.crm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.silexa.crm.screens.LeadsScreen
import com.silexa.crm.screens.LoginScreen
import com.silexa.crm.ui.theme.CRMTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "CRM_CALL"
    }

    // ============================================================
    // 1. CALL LOG PERMISSION
    // ============================================================

    private val callLogPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                Log.d(TAG, "READ_CALL_LOG permission GRANTED")
            } else {
                Log.d(TAG, "READ_CALL_LOG permission DENIED")
            }

            // IMPORTANT:
            // Only after the Call Log permission dialog has finished
            // do we request the next permission.
            requestPhoneStatePermission()
        }

    private fun requestCallLogPermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            Log.d(
                TAG,
                "READ_CALL_LOG permission already granted"
            )

            // Already granted → continue to next permission
            requestPhoneStatePermission()

            return
        }

        Log.d(
            TAG,
            "Requesting READ_CALL_LOG permission"
        )

        callLogPermissionLauncher.launch(
            Manifest.permission.READ_CALL_LOG
        )
    }


    // ============================================================
    // 2. PHONE STATE PERMISSION
    // ============================================================

    private val phoneStatePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                Log.d(
                    TAG,
                    "READ_PHONE_STATE permission GRANTED"
                )
            } else {
                Log.d(
                    TAG,
                    "READ_PHONE_STATE permission DENIED"
                )
            }

            // IMPORTANT:
            // Only after Phone State permission callback returns
            // do we move to recording permission.
            requestRecordingPermission()
        }

    private fun requestPhoneStatePermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            Log.d(
                TAG,
                "READ_PHONE_STATE permission already granted"
            )

            // Already granted → continue
            requestRecordingPermission()

            return
        }

        Log.d(
            TAG,
            "Requesting READ_PHONE_STATE permission"
        )

        phoneStatePermissionLauncher.launch(
            Manifest.permission.READ_PHONE_STATE
        )
    }


    // ============================================================
    // 3. RECORDING / MEDIA PERMISSION
    // ============================================================

    private val recordingPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                Log.d(
                    TAG,
                    "READ_MEDIA_AUDIO permission GRANTED"
                )

            } else {

                Log.d(
                    TAG,
                    "READ_MEDIA_AUDIO permission DENIED"
                )
            }

            // Recording permission is finished.
            // Now we can check the legacy storage permission.
            requestStoragePermission()
        }

    private fun requestRecordingPermission() {

        // READ_MEDIA_AUDIO exists only on Android 13+
        if (android.os.Build.VERSION.SDK_INT < 33) {

            Log.d(
                TAG,
                "Android < 13 → READ_MEDIA_AUDIO not required"
            )

            requestStoragePermission()

            return
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            Log.d(
                TAG,
                "READ_MEDIA_AUDIO permission already granted"
            )

            requestStoragePermission()

            return
        }

        Log.d(
            TAG,
            "Requesting READ_MEDIA_AUDIO permission"
        )

        recordingPermissionLauncher.launch(
            Manifest.permission.READ_MEDIA_AUDIO
        )
    }


    // ============================================================
    // 4. EXTERNAL STORAGE PERMISSION
    // ============================================================

    private val storagePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                Log.d(
                    TAG,
                    "READ_EXTERNAL_STORAGE permission GRANTED"
                )

            } else {

                Log.d(
                    TAG,
                    "READ_EXTERNAL_STORAGE permission DENIED"
                )
            }

            Log.d(
                TAG,
                "PERMISSION FLOW COMPLETED"
            )
        }

    private fun requestStoragePermission() {

        // READ_EXTERNAL_STORAGE is relevant only up to Android 12
        if (android.os.Build.VERSION.SDK_INT >= 33) {

            Log.d(
                TAG,
                "Android 13+ → READ_EXTERNAL_STORAGE not required"
            )

            Log.d(
                TAG,
                "PERMISSION FLOW COMPLETED"
            )

            return
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            Log.d(
                TAG,
                "READ_EXTERNAL_STORAGE permission already granted"
            )

            Log.d(
                TAG,
                "PERMISSION FLOW COMPLETED"
            )

            return
        }

        Log.d(
            TAG,
            "Requesting READ_EXTERNAL_STORAGE permission"
        )

        storagePermissionLauncher.launch(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }


    // ============================================================
    // ACTIVITY
    // ============================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        Log.d(
            TAG,
            "MAIN ACTIVITY CREATED"
        )

        setContent {

            CRMTheme {

                var showLeads by remember {
                    mutableStateOf(false)
                }

                AnimatedContent(
                    targetState = showLeads,
                    transitionSpec = {

                        if (targetState) {

                            (
                                    slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec = tween(400)
                                    ) +
                                            fadeIn(
                                                animationSpec = tween(300)
                                            )
                                    ).togetherWith(

                                    slideOutHorizontally(
                                        targetOffsetX = { -it / 3 },
                                        animationSpec = tween(350)
                                    ) +
                                            fadeOut(
                                                animationSpec = tween(250)
                                            )
                                )

                        } else {

                            (
                                    slideInHorizontally(
                                        initialOffsetX = { -it / 3 },
                                        animationSpec = tween(400)
                                    ) +
                                            fadeIn(
                                                animationSpec = tween(300)
                                            )
                                    ).togetherWith(

                                    slideOutHorizontally(
                                        targetOffsetX = { it },
                                        animationSpec = tween(350)
                                    ) +
                                            fadeOut(
                                                animationSpec = tween(250)
                                            )
                                )
                        }
                    },
                    label = "screenTransition"
                ) { destination ->

                    if (destination) {

                        LeadsScreen()

                    } else {

                        LoginScreen(
                            onLoginClick = {
                                showLeads = true
                            }
                        )
                    }
                }
            }
        }

        // ========================================================
        // START ONLY THE FIRST PERMISSION
        // ========================================================
        //
        // DO NOT start the other permissions here.
        //
        requestCallLogPermission()
    }
}