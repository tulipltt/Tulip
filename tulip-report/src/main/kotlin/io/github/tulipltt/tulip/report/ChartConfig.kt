package io.github.tulipltt.tulip.report

/**
 * Configuration for chart rendering.
 */
data class ChartConfig(
    val type: String,
    val id: String,
    val labels: List<String>,
    val data: List<List<Double?>>,
    val title: String,
    val unit: String,
)
