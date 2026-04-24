package io.github.tulipltt.tulip.report

/**
 * Provides the CSS styles used in the Tulip performance report.
 * This includes layout, typography, and theme-specific adjustments.
 */
object ReportStyles {
    private fun loadResource(name: String): String {
        return ReportStyles::class.java.getResource(name)?.readText()
            ?: throw IllegalStateException("Resource $name not found")
    }

    /**
     * The Pico CSS library.
     */
    val picoCss: String = loadResource("pico.min.css")

    /**
     * The complete CSS bundle for the report.
     */
    val styles: String = loadResource("styles.css")
}
