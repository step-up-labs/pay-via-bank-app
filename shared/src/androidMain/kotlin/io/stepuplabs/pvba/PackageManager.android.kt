package io.stepuplabs.pvba

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

fun PackageManager.intentReceivers(intent: Intent?): List<String> {
    intent ?: return emptyList()

    val queried =
        if (isAtLeastTiramisu) queryIntentActivities(
            intent,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
        )
        else queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )

    return queried.map { it.activityInfo.packageName }.distinct()
}

val isAtLeastTiramisu: Boolean
    get() = !isPreTiramisu

val isPreTiramisu: Boolean
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU