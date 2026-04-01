package io.github.tulipltt.tulip.report

import io.github.tulipltt.tulip.core.TulipConfig
import kotlinx.html.*
import org.HdrHistogram.Histogram
import java.nio.ByteBuffer
import java.util.*

fun FlowContent.statsCard(titleText: String, classes: String? = null, isChart: Boolean = false, block: DIV.() -> Unit) {
    val cardClasses = if (classes != null) "card $classes" else "card"
    div(classes = cardClasses) {
        div(classes = "card-header") {
            span { +titleText }
            if (isChart) {
                div(classes = "chart-controls") {
                    span(classes = "zoom-info") { +"Mouse: Pan/Zoom • Toolbox: Reset" }
                    button(classes = "btn-icon btn-fullscreen") {
                        title = "Toggle Fullscreen"
                        attributes["onclick"] = "toggleFullscreen(this.parentElement.parentElement.nextElementSibling.id)"
                        +"盍"
                    }
                }
            }
        }
        block()
    }
}

fun FlowContent.metadataSection(label: String, value: String) {
    div(classes = "metadata-item") {
        span(classes = "metadata-label") { +label }
        span(classes = "metadata-value") { +value }
    }
}

fun FlowContent.summaryTable(groupedResults: Map<String, List<BenchmarkResult>>) {
    div(classes = "stats-table-wrapper") {
        style = "margin-top: 24px;"
        table(classes = "stats-table") {
            thead {
                tr {
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
                    tr {
                        style = "background-color: rgba(199, 53, 247, 0.15); color: var(--accent-color); font-weight: bold; border-top: 2px solid var(--accent-color);"
                        td { 
                            a(href = "#benchmark_$bmId") {
                                style = "color: var(--accent-color); text-decoration: none;"
                                +name 
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
                            style = "border-bottom: 1px solid var(--border-color);"
                            td { 
                                style = "padding-left: 25px; color: var(--text-muted);"
                                +actionName 
                            }
                            td(classes = "numeric") { style = "color: var(--text-muted);"; +"-" }
                            td(classes = "numeric") { style = "color: var(--text-muted);"; +actionSummary.numActions.toString() }
                            td(classes = "numeric") { statusPill(actionSummary.numFailed.toLong()) }
                            td(classes = "numeric") { style = "color: var(--text-muted);"; +"%.1f".format(actionSummary.avgAps) }
                            td(classes = "numeric") { style = "color: var(--text-muted);"; +formatDuration(actionSummary.avgRt) }
                            td(classes = "numeric") { style = "color: var(--text-muted);"; +formatDuration(actionSummary.p50) }
                            td(classes = "numeric") { style = "color: var(--text-muted);"; +formatDuration(actionSummary.p90) }
                            td(classes = "numeric") { style = "color: var(--text-muted);"; +formatDuration(actionSummary.p99) }
                            td(classes = "numeric") { style = "color: var(--text-muted);"; +formatDuration(actionSummary.maxRt) }
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
        table(classes = "stats-table") {
            thead {
                tr {
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
                    tr {
                        style = "background-color: rgba(199, 53, 247, 0.15); color: var(--accent-color); font-weight: bold; border-top: 1px solid var(--border-color);"
                        td { +"Summary: $actionName" }
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
                                    style = "padding-left: 25px; color: var(--text-muted);"
                                    +"Iteration ${res.rowId + 1}"
                                }
                                td(classes = "numeric") { style = "color: var(--text-muted);"; +actionStats.numActions.toString() }
                                td(classes = "numeric") { statusPill(actionStats.numFailed.toLong()) }
                                td(classes = "numeric") { style = "color: var(--text-muted);"; +"%.1f".format(actionStats.avgAps) }
                                td(classes = "numeric") { style = "color: var(--text-muted);"; +formatDuration(actionStats.avgRt) }
                                td(classes = "numeric") { style = "color: var(--text-muted);"; +formatDuration(actionStats.percentilesRt["50.0"] ?: 0.0) }
                                td(classes = "numeric") { style = "color: var(--text-muted);"; +formatDuration(actionStats.percentilesRt["90.0"] ?: 0.0) }
                                td(classes = "numeric") { style = "color: var(--text-muted);"; +formatDuration(actionStats.percentilesRt["99.0"] ?: 0.0) }
                                td(classes = "numeric") { style = "color: var(--text-muted);"; +formatDuration(actionStats.maxRt) }
                            }
                        }
                    }
                }
                
                val summary = aggregateResults(results)
                // Overall Benchmark Row
                tr {
                    style = "background-color: rgba(199, 53, 247, 0.15); color: var(--accent-color); font-weight: 800; border-top: 2px solid var(--accent-color);"
                    td { +"OVERALL BENCHMARK" }
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
        span(classes = "status-pill status-failed") { +failed.toString() }
    } else {
        span(classes = "status-pill status-success") { +"0" }
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

fun FlowContent.configSection(config: TulipConfig) {
    statsCard(titleText = "General Config", classes = "full-width") {
        val actions = config.actions
        div(classes = "stats-table-wrapper") {
            table(classes = "stats-table") {
                tbody {
                    tr { td { +"Description" }; td { +actions.description } }
                    tr { td { +"User Class" }; td { +actions.userClass } }
                    tr { td { +"Output File" }; td { +actions.jsonFilename } }
                    tr { td { +"Report File" }; td { +actions.htmlFilename } }
                }
            }
        }
        
        h3(classes = "action-title") { +"User Parameters" }
        div(classes = "stats-table-wrapper") {
            table(classes = "stats-table") {
                thead { tr { th { +"Key" }; th { +"Value" } } }
                tbody {
                    actions.userParams.forEach { (k, v) ->
                        tr { td { +k }; td { +v.toString() } }
                    }
                }
            }
        }

        h3(classes = "action-title") { +"User Actions" }
        div(classes = "stats-table-wrapper") {
            table(classes = "stats-table") {
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
        statsCard(titleText = "Contexts", classes = "full-width") {
            config.contexts.forEach { (name, ctx) ->
                h3(classes = "action-title") { +name }
                div(classes = "stats-table-wrapper") {
                    table(classes = "stats-table") {
                        tbody {
                            tr { td { +"Enabled" }; td { +ctx.enabled.toString() } }
                            tr { td { +"Num Users" }; td { +ctx.numUsers.toString() } }
                        }
                    }
                }
            }
        }
    }

    statsCard(titleText = "Benchmark Configurations", classes = "full-width") {
        config.benchmarks.forEach { (name, cfg) ->
            h3(classes = "action-title") { +name }
            div(classes = "stats-table-wrapper") {
                table(classes = "stats-table") {
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
        statsCard(titleText = "Workflows", classes = "full-width") {
            config.workflows.forEach { (name, flow) ->
                h3(classes = "action-title") { +name }
                div(classes = "stats-table-wrapper") {
                    table(classes = "stats-table") {
                        thead { tr { th { +"From ID" }; th { +"To ID (Weight)" } } }
                        tbody {
                            flow.forEach { (fromId, transitions) ->
                                tr {
                                    td { +fromId }
                                    td {
                                        transitions.forEach { (toId, weight) ->
                                            span { 
                                                style = "margin-right: 15px;"
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

fun FlowContent.runtimeSection(reportData: ReportData) {
    statsCard(titleText = "Tulip Runtime Information", classes = "full-width") {
        val java = reportData.java
        
        h3(classes = "action-title") { +"Java System Properties" }
        div(classes = "stats-table-wrapper") {
            table(classes = "stats-table") {
                tbody {
                    java.systemProperties.forEach { (k, v) ->
                        tr { td { +k }; td { +v } }
                    }
                }
            }
        }

        h3(classes = "action-title") { +"Java Runtime Options" }
        div(classes = "stats-table-wrapper") {
            table(classes = "stats-table") {
                tbody {
                    java.runtimeOptions.forEach { opt: String ->
                        tr { td(classes = "numeric") { style = "text-align: left;"; +"Option" }; td { 
                            style = "word-break: break-all; font-family: 'JetBrains Mono', monospace; font-size: 0.75rem;"
                            +opt 
                        } }
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
