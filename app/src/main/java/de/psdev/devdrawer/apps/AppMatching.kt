package de.psdev.devdrawer.apps

import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.appwidget.PackageHashInfo
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.database.PackageFilter

/** The packages a profile shows: those matching at least one of its [filters], each once. */
fun List<PackageHashInfo>.matching(filters: List<PackageFilter>): List<PackageHashInfo> = filter { packageInfo ->
    filters.any { it.matches(packageInfo) }
}.distinctBy { it.packageName }

fun SortOrder.comparator(): Comparator<AppInfo> = when (this) {
    SortOrder.FIRST_INSTALLED -> compareByDescending { it.firstInstallTime }
    SortOrder.LAST_UPDATED -> compareByDescending { it.lastUpdateTime }
    SortOrder.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
    SortOrder.PACKAGE_NAME -> compareBy { it.packageName }
}
