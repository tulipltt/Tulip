package io.github.tulipltt.tulip.report

/**
 * Provides the JavaScript code used in the Tulip performance report.
 * This includes logic for ECharts initialization, theme toggling,
 * chart interactivity, and data export.
 */
object ReportScripts {
    private fun loadResource(name: String): String {
        return ReportScripts::class.java.getResource(name)?.readText()
            ?: error("Resource $name not found")
    }


    /**
     * The ECharts library.
     */
    val echartsJs: String = loadResource("echarts.min.js")

    /**
     * The complete JavaScript bundle for the report.
     */
    val scripts: String = loadResource("scripts.js")
}
