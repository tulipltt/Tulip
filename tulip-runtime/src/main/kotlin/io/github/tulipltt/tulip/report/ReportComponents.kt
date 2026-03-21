package io.github.tulipltt.tulip.report

import kotlinx.html.*
import org.HdrHistogram.Histogram
import java.nio.ByteBuffer
import java.util.*
import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import com.sun.management.OperatingSystemMXBean as SunOperatingSystemMXBean

fun FlowContent.statsCard(title: String, classes: String? = null, isChart: Boolean = false, block: DIV.() -> Unit) {
    val cardClasses = if (classes != null) "card $classes" else "card"
    div(classes = cardClasses) {
        div(classes = "card-header") {
            span { +title }
            if (isChart) {
                div {
                    span(classes = "text-muted") { 
                        style = "font-size: 0.7rem; font-weight: normal; margin-right: 15px;"
                        +"Drag to zoom • Right-click to reset"
                    }
                    button(classes = "btn-save") {
                        this.title = "Save as Image"
                        style = "background: none; border: none; color: var(--accent-color); cursor: pointer; padding: 0; font-size: 1rem;"
                        attributes["onclick"] = "downloadChart(this.parentElement.parentElement.nextElementSibling.id, '$title')"
                        +"💾" 
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
                    
                    // Benchmark Header Row (PURPLE background, WHITE text)
                    tr {
                        style = "background-color: #5E17A8; color: #ffffff; font-weight: bold; border-top: 2px solid var(--accent-color);"
                        td { 
                            a(href = "#benchmark_$bmId") {
                                style = "color: #ffffff; text-decoration: none;"
                                +name 
                            }
                        }
                        td(classes = "numeric") { +summary.numUsers.toString() }
                        td(classes = "numeric") { +summary.numActions.toString() }
                        td(classes = "numeric") { statusPill(summary.numFailed) }
                        td(classes = "numeric") { +"%.1f".format(summary.avgAps) }
                        td(classes = "numeric") { +formatDuration(summary.avgRt) }
                        td(classes = "numeric") { +formatDuration(summary.p50) }
                        td(classes = "numeric") { +formatDuration(summary.p90) }
                        td(classes = "numeric") { +formatDuration(summary.p99) }
                        td(classes = "numeric") { +formatDuration(summary.maxRt) }
                    }

                    val allActionNames = results.flatMap { it.userActions.values.map { a -> a.name } }.distinct().sorted()
                    allActionNames.forEach { actionName ->
                        val actionResults = results.mapNotNull { it.userActions.values.find { a -> a.name == actionName } }
                        val actionSummary = aggregateActionResults(actionName, actionResults)
                        // Action Summary Row (Semi-transparent PURPLE background, WHITE text)
                        tr {
                            style = "background-color: rgba(94, 23, 168, 0.2); color: #ffffff; border-bottom: 1px solid var(--border-color);"
                            td { 
                                style = "padding-left: 25px;"
                                +actionName 
                            }
                            td(classes = "numeric") { +"-" }
                            td(classes = "numeric") { +actionSummary.numActions.toString() }
                            td(classes = "numeric") { statusPill(actionSummary.numFailed) }
                            td(classes = "numeric") { +"%.1f".format(actionSummary.avgAps) }
                            td(classes = "numeric") { +formatDuration(actionSummary.avgRt) }
                            td(classes = "numeric") { +formatDuration(actionSummary.p50) }
                            td(classes = "numeric") { +formatDuration(actionSummary.p90) }
                            td(classes = "numeric") { +formatDuration(actionSummary.p99) }
                            td(classes = "numeric") { +formatDuration(actionSummary.maxRt) }
                        }
                    }
                }
            }
        }
    }
}

fun FlowContent.detailedBenchmarkTable(results: List<BenchmarkResult>) {
    val allActionNames = results.flatMap { it.userActions.values.map { a -> a.name } }.distinct().sorted()
    
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
                    
                    // Action Summary Row (Semi-transparent PURPLE background, WHITE text)
                    tr {
                        style = "background-color: rgba(94, 23, 168, 0.2); color: #ffffff; font-weight: bold; border-top: 1px solid var(--border-color);"
                        td { +"Summary: $actionName" }
                        td(classes = "numeric") { +actionSummary.numActions.toString() }
                        td(classes = "numeric") { statusPill(actionSummary.numFailed) }
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
                                td(classes = "numeric") { +actionStats.numActions.toString() }
                                td(classes = "numeric") { statusPill(actionStats.numFailed) }
                                td(classes = "numeric") { +"%.1f".format(actionStats.avgAps) }
                                td(classes = "numeric") { +formatDuration(actionStats.avgRt) }
                                td(classes = "numeric") { +formatDuration(actionStats.percentilesRt["50.0"] ?: 0.0) }
                                td(classes = "numeric") { +formatDuration(actionStats.percentilesRt["90.0"] ?: 0.0) }
                                td(classes = "numeric") { +formatDuration(actionStats.percentilesRt["99.0"] ?: 0.0) }
                                td(classes = "numeric") { +formatDuration(actionStats.maxRt) }
                            }
                        }
                    }
                }
                
                val summary = aggregateResults(results)
                // Overall Benchmark Row (PURPLE background, WHITE text)
                tr {
                    style = "background-color: #5E17A8; color: #ffffff; font-weight: 800; border-top: 2px solid var(--accent-color);"
                    td { +"OVERALL BENCHMARK" }
                    td(classes = "numeric") { +summary.numActions.toString() }
                    td(classes = "numeric") { statusPill(summary.numFailed) }
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
    
    val totalActions = results.sumOf { it.numActions }
    val totalFailed = results.sumOf { it.numFailed }
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

fun aggregateActionResults(name: String, actionStatsList: List<ActionStats>): AggregatedStats {
    if (actionStatsList.isEmpty()) return AggregatedStats(0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    
    val totalActions = actionStatsList.sumOf { it.numActions }
    val totalFailed = actionStatsList.sumOf { it.numFailed }
    
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

fun FlowContent.configSection(config: Config) {
    statsCard(title = "General Config", classes = "full-width") {
        val actions = config.actions
        if (actions != null) {
            div(classes = "stats-table-wrapper") {
                table(classes = "stats-table") {
                    tbody {
                        tr { td { +"Description" }; td { +(actions.description ?: "") } }
                        tr { td { +"User Class" }; td { +(actions.userClass ?: "") } }
                        tr { td { +"Output File" }; td { +(actions.outputFilename ?: "") } }
                        tr { td { +"Report File" }; td { +(actions.reportFilename ?: "") } }
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
                            tr { td { +k }; td { +v } }
                        }
                    }
                }
            }
        }
    }

    if (config.contexts.isNotEmpty()) {
        statsCard(title = "Contexts", classes = "full-width") {
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

    statsCard(title = "Benchmark Configurations", classes = "full-width") {
        config.benchmarks.forEach { (name, cfg) ->
            h3(classes = "action-title") { +name }
            div(classes = "stats-table-wrapper") {
                table(classes = "stats-table") {
                    tbody {
                        tr { td { +"Enabled" }; td { +cfg.enabled.toString() } }
                        cfg.apsRate?.let { tr { td { +"APS Rate" }; td { +"%.1f".format(it) } } }
                        cfg.apsRateStepChange?.let { tr { td { +"APS Rate Step Change" }; td { +"%.1f".format(it) } } }
                        cfg.apsRateStepCount?.let { tr { td { +"APS Rate Step Count" }; td { +it.toString() } } }
                        cfg.scenarioWorkflow?.let { tr { td { +"Scenario Workflow" }; td { +it } } }
                        if (cfg.scenarioActions.isNotEmpty()) {
                            tr { 
                                td { +"Scenario Actions" }
                                td { 
                                    cfg.scenarioActions.forEach { actionMap ->
                                        actionMap.forEach { (id, weight) ->
                                            div { +"ID: $id, Weight: $weight" }
                                        }
                                    }
                                }
                            }
                        }
                        cfg.time?.let { t ->
                            tr { td { +"Warmup 1 Duration" }; td { +t.warmupDuration1.toString() } }
                            tr { td { +"Warmup 2 Duration" }; td { +t.warmupDuration2.toString() } }
                            tr { td { +"Benchmark Duration" }; td { +t.benchmarkDuration.toString() } }
                            tr { td { +"Benchmark Iterations" }; td { +t.benchmarkIterations.toString() } }
                        }
                    }
                }
            }
        }
    }

    if (config.workflows.isNotEmpty()) {
        statsCard(title = "Workflows", classes = "full-width") {
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
    statsCard(title = "Tulip Runtime Information", classes = "full-width") {
        val firstResult = reportData.results.firstOrNull()
        val java = firstResult?.java
        val props = java?.jvmSystemProperties ?: emptyMap()
        
        div(classes = "stats-table-wrapper") {
            table(classes = "stats-table") {
                tbody {
                    // Java & OS Info from Properties
                    tr { td { +"Java Vendor" }; td { +(props["java.vendor"] ?: java?.javaVendor ?: "N/A") } }
                    tr { td { +"Java Version" }; td { +(props["java.version"] ?: java?.javaRuntimeVersion ?: "N/A") } }
                    tr { td { +"OS Name" }; td { +(props["os.name"] ?: "N/A") } }
                    tr { td { +"OS Architecture" }; td { +(props["os.arch"] ?: "N/A") } }
                    
                    // JVM Runtime Options
                    if (java?.jvmRuntimeOptions?.isNotEmpty() == true) {
                        tr { 
                            td { +"JVM Runtime Options" }
                            td { 
                                style = "word-break: break-all; font-family: 'JetBrains Mono', monospace; font-size: 0.75rem;"
                                +java.jvmRuntimeOptions.joinToString(" ") 
                            } 
                        }
                    }
                    
                    // JVM Memory from report JSON
                    tr { td { +"JVM Memory Total" }; td { +"${(firstResult?.jvmMemoryTotal ?: 0) / 1024 / 1024} MB" } }
                    tr { td { +"JVM Memory Maximum" }; td { +"${(firstResult?.jvmMemoryMaximum ?: 0) / 1024 / 1024} MB" } }
                    
                    // Process Info from report JSON
                    tr { td { +"Process CPU Cores" }; td { +(firstResult?.processCpuCores?.toString() ?: "N/A") } }
                }
            }
        }
    }
}

fun FlowContent.actionsTable(actions: Map<String, ActionStats>) {
    div(classes = "stats-table-wrapper") {
        table(classes = "stats-table") {
            thead {
                tr {
                    th { +"Action" }
                    th { +"# Count" }
                    th { +"# Failed" }
                    th { +"Avg APS" }
                    th { +"Avg RT" }
                    th { +"Min" }
                    th { +"p50" }
                    th { +"p90" }
                    th { +"p99" }
                    th { +"Max" }
                }
            }
            tbody {
                actions.values.sortedBy { it.name }.forEach { action ->
                    tr {
                        td { +action.name }
                        td(classes = "numeric") { +action.numActions.toString() }
                        td(classes = "numeric") { statusPill(action.numFailed) }
                        td(classes = "numeric") { +"%.1f".format(action.avgAps) }
                        td(classes = "numeric") { +formatDuration(action.avgRt) }
                        td(classes = "numeric") { +formatDuration(action.minRt) }
                        td(classes = "numeric") { +formatDuration(action.percentilesRt["50.0"] ?: 0.0) }
                        td(classes = "numeric") { +formatDuration(action.percentilesRt["90.0"] ?: 0.0) }
                        td(classes = "numeric") { +formatDuration(action.percentilesRt["99.0"] ?: 0.0) }
                        td(classes = "numeric") { +formatDuration(action.maxRt) }
                    }
                }
            }
        }
    }
}

private fun formatDuration(nanos: Double): String {
    return when {
        nanos < 1_000 -> "%.1f ns".format(nanos)
        nanos < 1_000_000 -> "%.1f µs".format(nanos / 1_000.0)
        nanos < 1_000_000_000 -> "%.1f ms".format(nanos / 1_000_000.0)
        else -> "%.2f s".format(nanos / 1_000_000_000.0)
    }
}
