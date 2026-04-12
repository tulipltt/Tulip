package io.github.tulipltt.tulip.report

/**
 * Provides the JavaScript code used in the Tulip performance report.
 * This includes logic for ECharts initialization, theme toggling,
 * chart interactivity, and data export.
 */
object ReportScripts {
    /**
     * The complete JavaScript bundle for the report.
     */
    val scripts: String by lazy {
        ReportScripts::class.java.getResource("scripts.js")?.readText()
            ?: error("Resource scripts.js not found")
    }
}
