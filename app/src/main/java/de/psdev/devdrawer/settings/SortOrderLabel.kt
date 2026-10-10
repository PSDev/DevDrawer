package de.psdev.devdrawer.settings

import de.psdev.devdrawer.appwidget.SortOrder

/**
 * The label for this order from `R.array.sort_order_labels`, which lists them in the order of
 * `R.array.sort_order_values`, not in [SortOrder]'s declaration order.
 */
fun SortOrder.label(labels: Array<String>): String = labels[LABEL_ORDER.indexOf(this)]

private val LABEL_ORDER = listOf(SortOrder.LAST_UPDATED, SortOrder.FIRST_INSTALLED, SortOrder.NAME, SortOrder.PACKAGE_NAME)
