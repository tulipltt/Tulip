package io.github.tulipltt.tulip.report

/**
 * Provides the CSS styles used in the Tulip performance report.
 * This includes layout, typography, and theme-specific adjustments.
 */
object ReportStyles {
    /**
     * The complete CSS bundle for the report.
     */
    val styles: String by lazy {
        ReportStyles::class.java.getResource("styles.css")?.readText()
            ?: error("Resource styles.css not found")
    }
}
