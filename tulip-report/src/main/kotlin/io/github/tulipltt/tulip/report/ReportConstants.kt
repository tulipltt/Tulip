package io.github.tulipltt.tulip.report

/**
 * Constants used throughout the report components.
 */
object ReportConstants {
    const val HISTOGRAM_PRECISION = 3
    const val PERCENTILE_CHART_MAX_X = 1000000.0
    const val NANOS_PER_MICRO = 1000.0
    const val NANOS_PER_MILLI = 1_000_000.0
    const val NANOS_PER_SEC = 1_000_000_000.0
    const val MILLIS_PER_SEC = 1000.0

    // Log-Linear Quantization points
    const val P0 = 0.0
    const val P50 = 50.0
    const val P75 = 75.0
    const val P90 = 90.0
    const val P95 = 95.0
    const val P99 = 99.0
    const val P99_9 = 99.9
    const val P99_99 = 99.99
    const val P99_999 = 99.999
    const val P99_9999 = 99.9999
    const val P100 = 100.0

    val LLQ_POINTS = listOf(P0, P50, P75, P90, P95, P99, P99_9, P99_99, P99_999, P99_9999, P100)

    // Percentile decimal points
    const val PERCENTILE_FORMAT_PRECISION = 5
    const val LATENCY_FORMAT_PRECISION = 12

    // UI Constants
    const val ACTION_INDENT_PX = 20
}
