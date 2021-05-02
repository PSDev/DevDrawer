package de.psdev.devdrawer.utils

import android.content.Intent
import android.content.pm.PackageManager
import java.text.Collator

/**
 * Method to get all apps installed and return as List
 */
fun PackageManager.getExistingPackages(): List<String> {
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    val activities = queryIntentActivities(intent, 0)

    val appSet = mutableSetOf<String>()

    activities.forEach { resolveInfo ->
        var packageName = resolveInfo.activityInfo.applicationInfo.packageName
        appSet.add(packageName)
        while (packageName.isNotEmpty()) {
            val lastIndex = packageName.lastIndexOf(".")
            if (lastIndex > 0) {
                packageName = packageName.substring(0, lastIndex)
                appSet.add("$packageName.*")
            } else {
                packageName = ""
            }
        }
    }

    return appSet.toList().sortedWith(Collator.getInstance())
}