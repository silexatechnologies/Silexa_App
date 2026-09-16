package com.silexa.crm.call

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.app.role.RoleManager

object CallSettings {

    /**
     * Checks whether SILEXA CRM is currently the default dialer.
     */
    fun isDefaultDialer(activity: Activity): Boolean {

        val telecomManager =
            activity.getSystemService(
                TelecomManager::class.java
            )

        return telecomManager.defaultDialerPackage ==
                activity.packageName
    }

    /**
     * Opens Android's screen to make SILEXA CRM
     * the default phone/dialer application.
     */
    fun requestDefaultDialer(activity: Activity) {

        if (isDefaultDialer(activity)) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val roleManager =
                activity.getSystemService(
                    RoleManager::class.java
                )

            if (
                roleManager != null &&
                roleManager.isRoleAvailable(
                    RoleManager.ROLE_DIALER
                )
            ) {

                val intent =
                    roleManager.createRequestRoleIntent(
                        RoleManager.ROLE_DIALER
                    )

                activity.startActivityForResult(
                    intent,
                    REQUEST_DEFAULT_DIALER
                )
            }

        } else {

            val intent = Intent(
                TelecomManager.ACTION_CHANGE_DEFAULT_DIALER
            )

            intent.putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                activity.packageName
            )

            activity.startActivityForResult(
                intent,
                REQUEST_DEFAULT_DIALER
            )
        }
    }

    const val REQUEST_DEFAULT_DIALER = 1001
}