package io.github.tulipltt.tulip.report

/**
 * Configuration for the [statsCard] component.
 */
data class StatsCardConfig(
    val titleText: String,
    val classes: String? = null,
    val isChart: Boolean = false,
    val tableId: String? = null,
)
