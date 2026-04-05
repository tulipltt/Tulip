package io.github.tulipltt.tulip.report

import io.github.tulipltt.tulip.core.TulipConfig
import kotlinx.html.*
import org.HdrHistogram.Histogram
import java.nio.ByteBuffer
import java.util.*

fun FlowContent.statsCard(titleText: String, isDark: Boolean = false, classes: String? = null, isChart: Boolean = false, isTable: Boolean = false, tableId: String? = null, block: DIV.() -> Unit) {
    article(classes = classes) {
        header {
            div(classes = "grid") {
                div { h5 { +titleText } }
                if (isChart) {
                    div { 
                        style = "text-align: right"
                        small { +"Use toolbox buttons: Pan/Zoom, Reset, Save" } 
                    }
                }
                if (isTable && tableId != null) {
                    div {
                        style = "text-align: right; display: flex; align-items: center; justify-content: flex-end; gap: 8px;"
                        val titleSlug = titleText.lowercase().replace(" ", "_").replace("/", "_")
                        
                        button(classes = "outline secondary contrast") {
                            style = "padding: 4px 8px; font-size: 0.8em; margin: 0; display: flex; align-items: center;"
                            attributes["onclick"] = "toggleFullscreen('$tableId')"
                            attributes["title"] = "Maximize Table"
                            unsafe { +ReportIcons.MAXIMIZE }
                        }

                        button(classes = "outline secondary contrast") {
                            style = "padding: 4px 8px; font-size: 0.8em; margin: 0;"
                            attributes["onclick"] = "downloadTableAsCSV('$tableId','${titleSlug}.csv')"
                            +"CSV"
                        }
                        button(classes = "outline secondary contrast") {
                            style = "padding: 4px 8px; font-size: 0.8em; margin: 0;"
                            attributes["onclick"] = "downloadTableAsJSON('$tableId','${titleSlug}.json')"
                            +"JSON"
                        }
                    }
                }
            }
        }
        div {
            block()
        }
    }
}

fun FlowContent.summaryTable(groupedResults: Map<String, List<BenchmarkResult>>, isDark: Boolean, tableId: String = "summaryTable") {
    div(classes = "overflow-auto") {
        table(classes = "striped") {
            id = tableId
            thead {
                tr {
                    th { +"Benchmark" }
                    th { +"Run Id" }
                    th { +"#N" }
                    th { +"#F" }
                    th { +"Time" }
                    th { +"APS" }
                    th { +"AVG_RT" }
                    th { +"SD_RT" }
                    th { +"MIN_RT" }
                    th { +"P50_RT" }
                    th { +"P90_RT" }
                    th { +"P95_RT" }
                    th { +"P99_RT" }
                    th { +"MAX_RT" }
                }
            }
            tbody {
                groupedResults.forEach { (name, results) ->
                    val bmId = name.replace(" ", "_")
                    val summary = aggregateResults(results)
                    
                    // Benchmark Header Row
                    tr {
                        td {
                            a(href = "#benchmark_$bmId") {
                                b { +name }
                            }
                        }
                        td(classes = "numeric") { +"-" }
                        td(classes = "numeric") { +summary.numActions.toString() }
                        td(classes = "numeric") { statusPill(summary.numFailed.toLong()) }
                        td(classes = "numeric") { +formatTime(summary.duration) }
                        td(classes = "numeric") { +"%.1f".format(summary.avgAps) }
                        td(classes = "numeric") { +formatDuration(summary.avgRt) }
                        td(classes = "numeric") { +formatDuration(summary.sdevRt) }
                        td(classes = "numeric") { +formatDuration(summary.minRt) }
                        td(classes = "numeric") { +formatDuration(summary.p50) }
                        td(classes = "numeric") { +formatDuration(summary.p90) }
                        td(classes = "numeric") { +formatDuration(summary.p95) }
                        td(classes = "numeric") { +formatDuration(summary.p99) }
                        td(classes = "numeric") { +formatDuration(summary.maxRt) }
                    }

                    val allActionNames = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                    allActionNames.forEach { actionName ->
                        val actionResults = results.mapNotNull { it.userActions.values.find { a -> a.name == actionName } }
                        val actionSummary = aggregateActionResults(actionName, actionResults, summary.duration)
                        // Action Row
                        tr {
                            td { 
                                div {
                                    style = "padding-left: 20px;"
                                    +actionName 
                                }
                            }
                            td(classes = "numeric") { style = "opacity: 0.5;"; +"-" }
                            td(classes = "numeric") { style = "opacity: 0.5;"; +actionSummary.numActions.toString() }
                            td(classes = "numeric") { statusPill(actionSummary.numFailed.toLong()) }
                            td(classes = "numeric") { style = "opacity: 0.5;"; +formatTime(actionSummary.duration) }
                            td(classes = "numeric") { style = "opacity: 0.5;"; +"%.1f".format(actionSummary.avgAps) }
                            td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionSummary.avgRt) }
                            td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionSummary.sdevRt) }
                            td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionSummary.minRt) }
                            td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionSummary.p50) }
                            td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionSummary.p90) }
                            td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionSummary.p95) }
                            td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionSummary.p99) }
                            td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionSummary.maxRt) }
                        }
                    }
                }
            }
        }
    }
}

fun FlowContent.detailedBenchmarkTable(results: List<BenchmarkResult>, tableId: String = "detailedTable") {
    val allActionNames = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
    
    div(classes = "overflow-auto") {
        table(classes = "striped") {
            id = tableId
            thead {
                tr {
                    th { +"Action / Iteration" }
                    th { +"Run Id" }
                    th { +"#N" }
                    th { +"#F" }
                    th { +"Time" }
                    th { +"APS" }
                    th { +"AVG_RT" }
                    th { +"SD_RT" }
                    th { +"MIN_RT" }
                    th { +"P50_RT" }
                    th { +"P90_RT" }
                    th { +"P95_RT" }
                    th { +"P99_RT" }
                    th { +"MAX_RT" }
                }
            }
            tbody {
                allActionNames.forEach { actionName ->
                    val actionResults = results.mapNotNull { it.userActions.values.find { a -> a.name == actionName } }
                    val totalDuration = results.sumOf { it.duration }
                    val actionSummary = aggregateActionResults(actionName, actionResults, totalDuration)
                    
                    // Action Summary Row
                    tr {
                        td { b { +"Summary: $actionName" } }
                        td(classes = "numeric") { +"-" }
                        td(classes = "numeric") { +actionSummary.numActions.toString() }
                        td(classes = "numeric") { statusPill(actionSummary.numFailed.toLong()) }
                        td(classes = "numeric") { +formatTime(actionSummary.duration) }
                        td(classes = "numeric") { +"%.1f".format(actionSummary.avgAps) }
                        td(classes = "numeric") { +formatDuration(actionSummary.avgRt) }
                        td(classes = "numeric") { +formatDuration(actionSummary.sdevRt) }
                        td(classes = "numeric") { +formatDuration(actionSummary.minRt) }
                        td(classes = "numeric") { +formatDuration(actionSummary.p50) }
                        td(classes = "numeric") { +formatDuration(actionSummary.p90) }
                        td(classes = "numeric") { +formatDuration(actionSummary.p95) }
                        td(classes = "numeric") { +formatDuration(actionSummary.p99) }
                        td(classes = "numeric") { +formatDuration(actionSummary.maxRt) }
                    }

                    results.forEach { res ->
                        val actionStats = res.userActions.values.find { it.name == actionName }
                        if (actionStats != null) {
                            tr {
                                td { 
                                    div {
                                        style = "padding-left: 20px;"
                                        +"Iteration ${res.rowId + 1}"
                                    }
                                }
                                td(classes = "numeric") { style = "opacity: 0.5;"; +(res.rowId + 1).toString() }
                                td(classes = "numeric") { style = "opacity: 0.5;"; +actionStats.numActions.toString() }
                                td(classes = "numeric") { statusPill(actionStats.numFailed.toLong()) }
                                td(classes = "numeric") { style = "opacity: 0.5;"; +formatTime(res.duration) }
                                td(classes = "numeric") { style = "opacity: 0.5;"; +"%.1f".format(actionStats.avgAps) }
                                td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionStats.avgRt) }
                                td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionStats.sdevRt) }
                                td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionStats.minRt) }
                                td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionStats.percentilesRt["50.0"] ?: 0.0) }
                                td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionStats.percentilesRt["90.0"] ?: 0.0) }
                                td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionStats.percentilesRt["95.0"] ?: 0.0) }
                                td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionStats.percentilesRt["99.0"] ?: 0.0) }
                                td(classes = "numeric") { style = "opacity: 0.5;"; +formatDuration(actionStats.maxRt) }
                            }
                        }
                    }
                }
                
                val summary = aggregateResults(results)
                // Overall Benchmark Row
                tr {
                    td { b { +"OVERALL BENCHMARK" } }
                    td(classes = "numeric") { +"-" }
                    td(classes = "numeric") { +summary.numActions.toString() }
                    td(classes = "numeric") { statusPill(summary.numFailed.toLong()) }
                    td(classes = "numeric") { +formatTime(summary.duration) }
                    td(classes = "numeric") { +"%.1f".format(summary.avgAps) }
                    td(classes = "numeric") { +formatDuration(summary.avgRt) }
                    td(classes = "numeric") { +formatDuration(summary.sdevRt) }
                    td(classes = "numeric") { +formatDuration(summary.minRt) }
                    td(classes = "numeric") { +formatDuration(summary.p50) }
                    td(classes = "numeric") { +formatDuration(summary.p90) }
                    td(classes = "numeric") { +formatDuration(summary.p95) }
                    td(classes = "numeric") { +formatDuration(summary.p99) }
                    td(classes = "numeric") { +formatDuration(summary.maxRt) }
                }
            }
        }
    }
}

private fun TD.statusPill(failed: Long) {
    if (failed > 0) {
        span(classes = "pill pill-fail") { +failed.toString() }
    } else {
        span(classes = "pill pill-pass") { +"0" }
    }
}

fun formatTime(seconds: Double): String {
    return "%.1f s".format(seconds)
}

fun FlowContent.llqPercentileTable(results: List<BenchmarkResult>, tableId: String) {
    val lastRes = results.last()
    div(classes = "overflow-auto") {
        style = "max-height: 400px;"
        table(classes = "striped") {
            id = tableId
            thead {
                tr {
                    th { +"Value" }
                    th { +"Percentile" }
                    th { +"Total Count" }
                    th { +"Bucket Size" }
                    th { +"Percentage" }
                    th { +"Above Count" }
                }
            }
            tbody {
                // Simplified LLQ representation based on report.py logic
                val distributionPoints = listOf(0.0, 50.0, 75.0, 90.0, 95.0, 99.0, 99.9, 99.99, 99.999, 100.0)
                val totalCount = lastRes.numActions.toLong()
                
                distributionPoints.forEach { p ->
                    val valueNanos = if (p == 100.0) lastRes.maxRt else lastRes.percentilesRt[p.toString()] ?: 0.0
                    val countAtP = (totalCount * (p / 100.0)).toLong()
                    tr {
                        td(classes = "numeric") { +formatDuration(valueNanos) }
                        td(classes = "numeric") { +"%.6f".format(p / 100.0) }
                        td(classes = "numeric") { +countAtP.toString() }
                        td(classes = "numeric") { +"-" }
                        td(classes = "numeric") { +"%.3f".format(p) }
                        td(classes = "numeric") { +(totalCount - countAtP).toString() }
                    }
                }
            }
        }
    }
}

fun FlowContent.hdrPercentileTable(results: List<BenchmarkResult>, tableId: String) {
    val lastRes = results.last()
    val base64 = lastRes.hdrHistogramRt ?: return
    val bytes = Base64.getDecoder().decode(base64)
    val histogram = Histogram.decodeFromCompressedByteBuffer(ByteBuffer.wrap(bytes), 0)

    div(classes = "overflow-auto") {
        style = "max-height: 400px;"
        table(classes = "striped") {
            id = tableId
            thead {
                tr {
                    th { +"Value" }
                    th { +"Percentile" }
                    th { +"TotalCount" }
                    th { +"1/(1-Percentile)" }
                    th { +"AboveCount" }
                }
            }
            tbody {
                val totalCount = histogram.totalCount
                // Matching report.py logic: output reverse order for high detail in tail
                val data = mutableListOf<Triple<Double, Double, Long>>()
                histogram.percentiles(5).forEach { iv ->
                    data.add(Triple(iv.percentileLevelIteratedTo, iv.valueIteratedTo.toDouble(), iv.totalCountToThisValue))
                }
                data.asReversed().forEach { (p, value, count) ->
                    val factor = if (p < 100.0) "%.2f".format(100.0 / (100.0 - p)) else ""
                    tr {
                        td(classes = "numeric") { +formatDuration(value) }
                        td(classes = "numeric") { +"%.12f".format(p / 100.0) }
                        td(classes = "numeric") { +count.toString() }
                        td(classes = "numeric") { +factor }
                        td(classes = "numeric") { +(totalCount - count).toString() }
                    }
                }
            }
        }
    }
}

data class AggregatedStats(
    val numUsers: Int,
    val numActions: Long,
    val numFailed: Long,
    val duration: Double,
    val avgAps: Double,
    val avgRt: Double,
    val sdevRt: Double,
    val minRt: Double,
    val p50: Double,
    val p90: Double,
    val p95: Double,
    val p99: Double,
    val maxRt: Double
)

fun aggregateResults(results: List<BenchmarkResult>): AggregatedStats {
    if (results.isEmpty()) return AggregatedStats(0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    
    val totalActions = results.sumOf { it.numActions.toLong() }
    val totalFailed = results.sumOf { it.numFailed.toLong() }
    val totalDuration = results.sumOf { it.duration }
    
    val aggregateHistogram = Histogram(3)
    results.forEach { res ->
        res.hdrHistogramRt?.let { base64 ->
            try {
                val bytes = Base64.getDecoder().decode(base64)
                val iterationHistogram = Histogram.decodeFromCompressedByteBuffer(ByteBuffer.wrap(bytes), 0)
                aggregateHistogram.add(iterationHistogram)
            } catch (e: Exception) {}
        }
    }

    return AggregatedStats(
        numUsers = results.maxOf { it.numUsers },
        numActions = totalActions,
        numFailed = totalFailed,
        duration = totalDuration,
        avgAps = if (totalDuration > 0) totalActions / totalDuration else 0.0,
        avgRt = aggregateHistogram.mean,
        sdevRt = aggregateHistogram.stdDeviation,
        minRt = aggregateHistogram.minValue.toDouble(),
        p50 = aggregateHistogram.getValueAtPercentile(50.0).toDouble(),
        p90 = aggregateHistogram.getValueAtPercentile(90.0).toDouble(),
        p95 = aggregateHistogram.getValueAtPercentile(95.0).toDouble(),
        p99 = aggregateHistogram.getValueAtPercentile(99.0).toDouble(),
        maxRt = aggregateHistogram.maxValue.toDouble()
    )
}

fun aggregateActionResults(name: String, actionStatsList: List<ActionStatResult>, totalDuration: Double): AggregatedStats {
    if (actionStatsList.isEmpty()) return AggregatedStats(0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    
    val totalActions = actionStatsList.sumOf { it.numActions.toLong() }
    val totalFailed = actionStatsList.sumOf { it.numFailed.toLong() }
    
    val aggregateHistogram = Histogram(3)
    actionStatsList.forEach { stat ->
        stat.hdrHistogramRt?.let { base64 ->
            try {
                val bytes = Base64.getDecoder().decode(base64)
                val iterationHistogram = Histogram.decodeFromCompressedByteBuffer(ByteBuffer.wrap(bytes), 0)
                aggregateHistogram.add(iterationHistogram)
            } catch (e: Exception) {}
        }
    }

    return AggregatedStats(
        numUsers = 0,
        numActions = totalActions,
        numFailed = totalFailed,
        duration = totalDuration,
        avgAps = if (totalDuration > 0) totalActions / totalDuration else 0.0,
        avgRt = aggregateHistogram.mean,
        sdevRt = aggregateHistogram.stdDeviation,
        minRt = aggregateHistogram.minValue.toDouble(),
        p50 = aggregateHistogram.getValueAtPercentile(50.0).toDouble(),
        p90 = aggregateHistogram.getValueAtPercentile(90.0).toDouble(),
        p95 = aggregateHistogram.getValueAtPercentile(95.0).toDouble(),
        p99 = aggregateHistogram.getValueAtPercentile(99.0).toDouble(),
        maxRt = aggregateHistogram.maxValue.toDouble()
    )
}

fun FlowContent.configSection(config: TulipConfig, isDark: Boolean = false) {
    statsCard(titleText = "General Config", isDark = isDark, classes = "full-width") {
        val actions = config.actions
        div(classes = "overflow-auto") {
            table {
                tbody {
                    tr { td { +"Description" }; td { +actions.description } }
                    tr { td { +"User Class" }; td { +actions.userClass } }
                    tr { td { +"Output File" }; td { +actions.jsonFilename } }
                    tr { td { +"Report File" }; td { +actions.htmlFilename } }
                }
            }
        }
        
        h5 { b { +"User Parameters" } }
        div(classes = "overflow-auto") {
            table {
                thead { tr { th { +"Key" }; th { +"Value" } } }
                tbody {
                    actions.userParams.forEach { (k, v) ->
                        tr { td { +k }; td { +v.toString() } }
                    }
                }
            }
        }

        h5 { b { +"User Actions" } }
        div(classes = "overflow-auto") {
            table {
                thead { tr { th { +"ID" }; th { +"Description" } } }
                tbody {
                    actions.userActions.forEach { (k, v) ->
                        tr { td { +k.toString() }; td { +v } }
                    }
                }
            }
        }
    }

    if (config.contexts.isNotEmpty()) {
        statsCard(titleText = "Contexts", isDark = isDark, classes = "full-width") {
            config.contexts.forEach { (name, ctx) ->
                h5 { b { +name } }
                div(classes = "overflow-auto") {
                    table {
                        tbody {
                            tr { td { +"Enabled" }; td { +ctx.enabled.toString() } }
                            tr { td { +"Num Users" }; td { +ctx.numUsers.toString() } }
                        }
                    }
                }
            }
        }
    }

    statsCard(titleText = "Benchmark Configurations", isDark = isDark, classes = "full-width") {
        config.benchmarks.forEach { (name, cfg) ->
            h5 { b { +name } }
            div(classes = "overflow-auto") {
                table {
                    tbody {
                        tr { td { +"Enabled" }; td { +cfg.enabled.toString() } }
                        tr { td { +"APS Rate" }; td { +"%.1f".format(cfg.throughputRate) } }
                        tr { td { +"Scenario Workflow" }; td { +cfg.workflow } }
                        if (cfg.actions.isNotEmpty()) {
                            tr { 
                                td { +"Scenario Actions" }
                                td { 
                                    cfg.actions.forEach { action ->
                                        div { +"ID: ${action.id}, Weight: ${action.weight}" }
                                    }
                                }
                            }
                        }
                        tr { td { +"Warmup 1 Duration" }; td { +cfg.startupDuration.toString() } }
                        tr { td { +"Warmup 2 Duration" }; td { +cfg.warmupDuration.toString() } }
                        tr { td { +"Benchmark Duration" }; td { +cfg.mainDuration.toString() } }
                        tr { td { +"Benchmark Iterations" }; td { +cfg.mainDurationRepeatCount.toString() } }
                    }
                }
            }
        }
    }

    if (config.workflows.isNotEmpty()) {
        statsCard(titleText = "Workflows", isDark = isDark, classes = "full-width") {
            config.workflows.forEach { (name, flow) ->
                h5 { b { +name } }
                div(classes = "overflow-auto") {
                    table {
                        thead { tr { th { +"From ID" }; th { +"To ID (Weight)" } } }
                        tbody {
                            flow.forEach { (fromId, transitions) ->
                                tr {
                                    td { +fromId }
                                    td {
                                        transitions.forEach { (toId, weight) ->
                                            span { 
                                                style = "margin-right: 16px;"
                                                +"$toId ($weight)" 
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun FlowContent.runtimeSection(reportData: ReportData, isDark: Boolean = false) {
    statsCard(titleText = "Tulip Runtime Information", isDark = isDark, classes = "full-width") {
        val java = reportData.java
        
        h5 { b { +"Java System Properties" } }
        div(classes = "overflow-auto") {
            table {
                tbody {
                    java.systemProperties.forEach { (k, v) ->
                        tr { td { +k }; td { +v } }
                    }
                }
            }
        }

        h5 { b { +"Java Runtime Options" } }
        div(classes = "overflow-auto") {
            table {
                tbody {
                    java.runtimeOptions.forEach { opt: String ->
                        tr { 
                            td(classes = "numeric") { style="text-align:left; opacity: 0.5;"; +"Option" }
                            td { 
                                style = "word-break: break-all;"
                                span(classes = "numeric") { +opt }
                            } 
                        }
                    }
                }
            }
        }
    }
}

fun formatDuration(nanos: Double): String {
    return when {
        nanos < 1_000 -> "%.1f ns".format(nanos)
        nanos < 1_000_000 -> "%.1f µs".format(nanos / 1_000.0)
        nanos < 1_000_000_000 -> "%.1f ms".format(nanos / 1_000_000.0)
        else -> "%.2f s".format(nanos / 1_000_000_000.0)
    }
}

object ReportIcons {
    private fun iconBase(path: String) = """
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="nav-icon">
            $path
        </svg>
    """.trimIndent()

    val DASHBOARD = iconBase("""<rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/>""")
    val ACTIVITY = iconBase("""<path d="M22 12h-4l-3 9L9 3l-3 9H2"/>""")
    val SETTINGS = iconBase("""<path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.38a2 2 0 0 0-.73-2.73l-.15-.1a2 2 0 0 1-1-1.72v-.51a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/>""")
    val INFO = iconBase("""<circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>""")
    val THEME = iconBase("""<path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>""")
    val MAXIMIZE = iconBase("""<path d="M15 3h6v6"/><path d="M9 21H3v-6"/><path d="M21 3l-7 7"/><path d="M3 21l7-7"/>""")
}
