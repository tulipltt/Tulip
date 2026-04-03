package io.github.tulipltt.tulip.report

import io.github.tulipltt.tulip.core.TulipConfig
import kotlinx.html.*
import org.HdrHistogram.Histogram
import java.nio.ByteBuffer
import java.util.*

fun FlowContent.statsCard(titleText: String, isDark: Boolean = false, classes: String? = null, isChart: Boolean = false, block: DIV.() -> Unit) {
    val cardBg = if (isDark) "w3-theme-d4" else "w3-theme-l5"
    val cardClasses = if (classes != null) "card w3-card $cardBg w3-margin-bottom $classes" else "card w3-card $cardBg w3-margin-bottom"
    div(classes = cardClasses) {
        val headerThemeClass = if (isDark) "w3-theme-d2" else "w3-theme-l2"
        header(classes = "w3-container $headerThemeClass") {
            div(classes = "w3-cell-row") {
                div(classes = "w3-cell w3-cell-middle") {
                    h5 { +titleText }
                }
                if (isChart) {
                    div(classes = "w3-cell w3-cell-middle w3-right-align") {
                        span(classes = "w3-tiny w3-opacity") { +"Use toolbox buttons: Pan/Zoom, Reset, Save, Fullscreen" }
                    }
                }
            }
        }
        div(classes = "w3-container w3-padding-16") {
            block()
        }
    }
}

fun FlowContent.summaryTable(groupedResults: Map<String, List<BenchmarkResult>>, isDark: Boolean) {
    val headerClass = if (isDark) "w3-theme-d3" else "w3-theme-l3"
    val rowClass = if (isDark) "w3-theme-d2" else "w3-theme-l3"
    div(classes = "stats-table-wrapper") {
        table(classes = "w3-table-all w3-hoverable") {
            thead {
                tr(classes = headerClass) {
                    th { +"Benchmark / Action" }
                    th { +"Users" }
                    th { +"# Actions" }
                    th { +"# Failed" }
                    th { +"Avg APS" }
                    th { +"Avg RT" }
                    th { +"p50" }
                    th { +"p90" }
                    th { +"p99" }
                    th { +"Max" }
                }
            }
            tbody {
                groupedResults.forEach { (name, results) ->
                    val bmId = name.replace(" ", "_")
                    val summary = aggregateResults(results)
                    
                    // Benchmark Header Row
                    tr(classes = rowClass) {
                        td {
                            a(href = "#benchmark_$bmId") {
                                b { +name }
                            }
                        }
                        td(classes = "numeric") { +summary.numUsers.toString() }
                        td(classes = "numeric") { +summary.numActions.toString() }
                        td(classes = "numeric") { statusPill(summary.numFailed.toLong()) }
                        td(classes = "numeric") { +"%.1f".format(summary.avgAps) }
                        td(classes = "numeric") { +formatDuration(summary.avgRt) }
                        td(classes = "numeric") { +formatDuration(summary.p50) }
                        td(classes = "numeric") { +formatDuration(summary.p90) }
                        td(classes = "numeric") { +formatDuration(summary.p99) }
                        td(classes = "numeric") { +formatDuration(summary.maxRt) }
                    }

                    val allActionNames = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                    allActionNames.forEach { actionName ->
                        val actionResults = results.mapNotNull { it.userActions.values.find { a -> a.name == actionName } }
                        val actionSummary = aggregateActionResults(actionName, actionResults)
                        // Action Row
                        tr {
                            td { 
                                div {
                                    style = "padding-left: 20px;"
                                    +actionName 
                                }
                            }
                            td(classes = "numeric w3-opacity") { +"-" }
                            td(classes = "numeric w3-opacity") { +actionSummary.numActions.toString() }
                            td(classes = "numeric") { statusPill(actionSummary.numFailed.toLong()) }
                            td(classes = "numeric w3-opacity") { +"%.1f".format(actionSummary.avgAps) }
                            td(classes = "numeric w3-opacity") { +formatDuration(actionSummary.avgRt) }
                            td(classes = "numeric w3-opacity") { +formatDuration(actionSummary.p50) }
                            td(classes = "numeric w3-opacity") { +formatDuration(actionSummary.p90) }
                            td(classes = "numeric w3-opacity") { +formatDuration(actionSummary.p99) }
                            td(classes = "numeric w3-opacity") { +formatDuration(actionSummary.maxRt) }
                        }
                    }
                }
            }
        }
    }
}

fun FlowContent.detailedBenchmarkTable(results: List<BenchmarkResult>) {
    val allActionNames = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
    
    div(classes = "stats-table-wrapper") {
        table(classes = "w3-table-all w3-hoverable") {
            thead {
                tr(classes = "w3-theme-d3") {
                    th { +"Action / Iteration" }
                    th { +"# Count" }
                    th { +"# Failed" }
                    th { +"Avg APS" }
                    th { +"Avg RT" }
                    th { +"p50" }
                    th { +"p90" }
                    th { +"p99" }
                    th { +"Max" }
                }
            }
            tbody {
                allActionNames.forEach { actionName ->
                    val actionResults = results.mapNotNull { it.userActions.values.find { a -> a.name == actionName } }
                    val actionSummary = aggregateActionResults(actionName, actionResults)
                    
                    // Action Summary Row
                    tr(classes = "w3-theme-l4") {
                        td { b { +"Summary: $actionName" } }
                        td(classes = "numeric") { +actionSummary.numActions.toString() }
                        td(classes = "numeric") { statusPill(actionSummary.numFailed.toLong()) }
                        td(classes = "numeric") { +"%.1f".format(actionSummary.avgAps) }
                        td(classes = "numeric") { +formatDuration(actionSummary.avgRt) }
                        td(classes = "numeric") { +formatDuration(actionSummary.p50) }
                        td(classes = "numeric") { +formatDuration(actionSummary.p90) }
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
                                td(classes = "numeric w3-opacity") { +actionStats.numActions.toString() }
                                td(classes = "numeric") { statusPill(actionStats.numFailed.toLong()) }
                                td(classes = "numeric w3-opacity") { +"%.1f".format(actionStats.avgAps) }
                                td(classes = "numeric w3-opacity") { +formatDuration(actionStats.avgRt) }
                                td(classes = "numeric w3-opacity") { +formatDuration(actionStats.percentilesRt["50.0"] ?: 0.0) }
                                td(classes = "numeric w3-opacity") { +formatDuration(actionStats.percentilesRt["90.0"] ?: 0.0) }
                                td(classes = "numeric w3-opacity") { +formatDuration(actionStats.percentilesRt["99.0"] ?: 0.0) }
                                td(classes = "numeric w3-opacity") { +formatDuration(actionStats.maxRt) }
                            }
                        }
                    }
                }
                
                val summary = aggregateResults(results)
                // Overall Benchmark Row
                tr(classes = "w3-theme-d1") {
                    td { b { +"OVERALL BENCHMARK" } }
                    td(classes = "numeric") { +summary.numActions.toString() }
                    td(classes = "numeric") { statusPill(summary.numFailed.toLong()) }
                    td(classes = "numeric") { +"%.1f".format(summary.avgAps) }
                    td(classes = "numeric") { +formatDuration(summary.avgRt) }
                    td(classes = "numeric") { +formatDuration(summary.p50) }
                    td(classes = "numeric") { +formatDuration(summary.p90) }
                    td(classes = "numeric") { +formatDuration(summary.p99) }
                    td(classes = "numeric") { +formatDuration(summary.maxRt) }
                }
            }
        }
    }
}

private fun TD.statusPill(failed: Long) {
    if (failed > 0) {
        span(classes = "w3-tag w3-round w3-red") { +failed.toString() }
    } else {
        span(classes = "w3-tag w3-round w3-theme") { +"0" }
    }
}

data class AggregatedStats(
    val numUsers: Int,
    val numActions: Long,
    val numFailed: Long,
    val avgAps: Double,
    val avgRt: Double,
    val p50: Double,
    val p90: Double,
    val p99: Double,
    val maxRt: Double
)

fun aggregateResults(results: List<BenchmarkResult>): AggregatedStats {
    if (results.isEmpty()) return AggregatedStats(0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    
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
        avgAps = if (totalDuration > 0) totalActions / totalDuration else 0.0,
        avgRt = aggregateHistogram.mean,
        p50 = aggregateHistogram.getValueAtPercentile(50.0).toDouble(),
        p90 = aggregateHistogram.getValueAtPercentile(90.0).toDouble(),
        p99 = aggregateHistogram.getValueAtPercentile(99.0).toDouble(),
        maxRt = aggregateHistogram.maxValue.toDouble()
    )
}

fun aggregateActionResults(name: String, actionStatsList: List<ActionStatResult>): AggregatedStats {
    if (actionStatsList.isEmpty()) return AggregatedStats(0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    
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
        avgAps = if (actionStatsList.size > 0) actionStatsList.sumOf { it.avgAps } / actionStatsList.size else 0.0,
        avgRt = aggregateHistogram.mean,
        p50 = aggregateHistogram.getValueAtPercentile(50.0).toDouble(),
        p90 = aggregateHistogram.getValueAtPercentile(90.0).toDouble(),
        p99 = aggregateHistogram.getValueAtPercentile(99.0).toDouble(),
        maxRt = aggregateHistogram.maxValue.toDouble()
    )
}

fun FlowContent.configSection(config: TulipConfig, isDark: Boolean = false) {
    statsCard(titleText = "General Config", isDark = isDark, classes = "full-width") {
        val actions = config.actions
        div(classes = "stats-table-wrapper") {
            table(classes = "w3-table-all") {
                tbody {
                    tr { td { +"Description" }; td { +actions.description } }
                    tr { td { +"User Class" }; td { +actions.userClass } }
                    tr { td { +"Output File" }; td { +actions.jsonFilename } }
                    tr { td { +"Report File" }; td { +actions.htmlFilename } }
                }
            }
        }
        
        h5(classes = "w3-text-theme w3-margin-top") { b { +"User Parameters" } }
        div(classes = "stats-table-wrapper") {
            table(classes = "w3-table-all") {
                thead { tr(classes = "w3-light-grey") { th { +"Key" }; th { +"Value" } } }
                tbody {
                    actions.userParams.forEach { (k, v) ->
                        tr { td { +k }; td { +v.toString() } }
                    }
                }
            }
        }

        h5(classes = "w3-text-theme w3-margin-top") { b { +"User Actions" } }
        div(classes = "stats-table-wrapper") {
            table(classes = "w3-table-all") {
                thead { tr(classes = "w3-light-grey") { th { +"ID" }; th { +"Description" } } }
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
                h5(classes = "w3-text-theme w3-margin-top") { b { +name } }
                div(classes = "stats-table-wrapper") {
                    table(classes = "w3-table-all") {
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
            h5(classes = "w3-text-theme w3-margin-top") { b { +name } }
            div(classes = "stats-table-wrapper") {
                table(classes = "w3-table-all") {
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
                h5(classes = "w3-text-theme w3-margin-top") { b { +name } }
                div(classes = "stats-table-wrapper") {
                    table(classes = "w3-table-all") {
                        thead { tr(classes = "w3-light-grey") { th { +"From ID" }; th { +"To ID (Weight)" } } }
                        tbody {
                            flow.forEach { (fromId, transitions) ->
                                tr {
                                    td { +fromId }
                                    td {
                                        transitions.forEach { (toId, weight) ->
                                            span(classes = "w3-margin-right") { 
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
        
        h5(classes = "w3-text-theme w3-margin-top") { b { +"Java System Properties" } }
        div(classes = "stats-table-wrapper") {
            table(classes = "w3-table-all") {
                tbody {
                    java.systemProperties.forEach { (k, v) ->
                        tr { td { +k }; td { +v } }
                    }
                }
            }
        }

        h5(classes = "w3-text-theme w3-margin-top") { b { +"Java Runtime Options" } }
        div(classes = "stats-table-wrapper") {
            table(classes = "w3-table-all") {
                tbody {
                    java.runtimeOptions.forEach { opt: String ->
                        tr { 
                            td(classes = "numeric w3-opacity") { style="text-align:left"; +"Option" }
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
