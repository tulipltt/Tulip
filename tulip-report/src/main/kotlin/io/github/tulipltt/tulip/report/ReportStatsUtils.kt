package io.github.tulipltt.tulip.report

import kotlinx.html.FlowContent
import kotlinx.html.span
import org.HdrHistogram.Histogram
import java.nio.ByteBuffer
import java.util.Base64

/**
 * Aggregates results across multiple iterations of a benchmark.
 */
fun aggregateResults(results: List<BenchmarkResult>): AggregatedStats {
    if (results.isEmpty()) return emptyStats()
    
    val totalActions = results.sumOf { it.numActions.toLong() }
    val totalFailed = results.sumOf { it.numFailed.toLong() }
    val totalDuration = results.sumOf { it.duration }
    
    val aggregateHistogram = Histogram(ReportConstants.HISTOGRAM_PRECISION)
    results.forEach { res ->
        decodeHistogram(res.hdrHistogramRt)?.let { iterationHistogram ->
            try {
                aggregateHistogram.add(iterationHistogram)
            } catch (ignore: Exception) { }
        }
    }

    return statsFromHistogram(
        results.maxOf { it.numUsers },
        totalActions,
        totalFailed,
        totalDuration,
        aggregateHistogram
    )
}

/**
 * Aggregates results for a specific action across multiple iterations.
 */
fun aggregateActionResults(
    actionStatsList: List<ActionStatResult>,
    totalDuration: Double
): AggregatedStats {
    if (actionStatsList.isEmpty()) return emptyStats()
    
    val totalActions = actionStatsList.sumOf { it.numActions.toLong() }
    val totalFailed = actionStatsList.sumOf { it.numFailed.toLong() }
    
    val aggregateHistogram = Histogram(ReportConstants.HISTOGRAM_PRECISION)
    actionStatsList.forEach { stat ->
        decodeHistogram(stat.hdrHistogramRt)?.let { iterationHistogram ->
            try {
                aggregateHistogram.add(iterationHistogram)
            } catch (ignore: Exception) { }
        }
    }

    return statsFromHistogram(0, totalActions, totalFailed, totalDuration, aggregateHistogram)
}

private fun emptyStats() = AggregatedStats(0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

/**
 * Decodes a base64 encoded HDR Histogram.
 */
fun decodeHistogram(base64: String?): Histogram? {
    if (base64 == null) return null
    return try {
        val bytes = Base64.getDecoder().decode(base64)
        Histogram.decodeFromCompressedByteBuffer(ByteBuffer.wrap(bytes), 0)
    } catch (ignore: Exception) {
        null
    }
}

private fun statsFromHistogram(
    numUsers: Int,
    actions: Long,
    failed: Long,
    duration: Double,
    h: Histogram
) = AggregatedStats(
    numUsers = numUsers,
    numActions = actions,
    numFailed = failed,
    duration = duration,
    avgAps = if (duration > 0) actions / duration else 0.0,
    avgRt = h.mean,
    sdevRt = h.stdDeviation,
    minRt = h.minValue.toDouble(),
    p50 = h.getValueAtPercentile(ReportConstants.P50).toDouble(),
    p90 = h.getValueAtPercentile(ReportConstants.P90).toDouble(),
    p95 = h.getValueAtPercentile(ReportConstants.P95).toDouble(),
    p99 = h.getValueAtPercentile(ReportConstants.P99).toDouble(),
    maxRt = h.maxValue.toDouble()
)

/**
 * Renders a status pill (pass/fail).
 */
fun FlowContent.statusPill(failed: Long) {
    if (failed > 0) {
        span(classes = "pill pill-fail") { +failed.toString() }
    } else {
        span(classes = "pill pill-pass") { +"0" }
    }
}
