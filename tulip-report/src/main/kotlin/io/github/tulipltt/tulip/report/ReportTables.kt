package io.github.tulipltt.tulip.report

import kotlinx.html.*

/**
 * Renders a summary table for all benchmarks.
 */
fun FlowContent.summaryTable(
    groupedResults: Map<String, List<BenchmarkResult>>,
    tableId: String = "summaryTable"
) {
    div(classes = "overflow-auto") {
        table(classes = "striped") {
            id = tableId
            renderTableHead("Benchmark")
            tbody {
                groupedResults.forEach { (name, results) ->
                    val bmId = name.replace(" ", "_")
                    val summary = aggregateResults(results)
                    renderSummaryRow(name, bmId, summary)

                    val actions = results.flatMap { 
                        it.userActions.values.map { a -> a.name ?: "" } 
                    }.distinct().sorted()
                    
                    actions.forEach { actionName ->
                        val actionResults = results.mapNotNull { 
                            it.userActions.values.find { a -> a.name == actionName } 
                        }
                        val actionSummary = aggregateActionResults(actionResults, summary.duration)
                        renderActionRow(actionName, actionSummary)
                    }
                }
            }
        }
    }
}

private fun TABLE.renderTableHead(firstColLabel: String) {
    thead {
        tr {
            th { +firstColLabel }; th { +"Run Id" }; th { +"Action Count" }; th { +"Failed" }; th { +"Time" }
            th { +"APS" }; th { +"Avg RT" }; th { +"Standard Deviation RT" }; th { +"Min RT" }; th { +"P50 RT" }
            th { +"P90 RT" }; th { +"P95 RT" }; th { +"P99 RT" }; th { +"MAX RT" }
        }
    }
}

private fun TBODY.renderSummaryRow(name: String, bmId: String, summary: AggregatedStats) {
    tr {
        td { a(href = "#benchmark_$bmId") { b { +name } } }
        renderStatsTDs(summary)
    }
}

private fun TBODY.renderActionRow(actionName: String, summary: AggregatedStats) {
    tr {
        td { 
            div {
                style = "padding-left: ${ReportConstants.ACTION_INDENT_PX}px;"
                +actionName 
            }
        }
        renderStatsTDs(summary, isMuted = true)
    }
}

private fun TR.renderStatsTDs(summary: AggregatedStats, isMuted: Boolean = false) {
    fun tdStats(value: String) {
        td(classes = "numeric") {
            if (isMuted) { style = "opacity: 0.5;" }
            +value
        }
    }

    tdStats("-")
    tdStats(summary.numActions.toString())
    td(classes = "numeric") { statusPill(summary.numFailed) }
    tdStats(formatTime(summary.duration))
    tdStats("%.1f".format(summary.avgAps))
    tdStats(formatDuration(summary.avgRt))
    tdStats(formatDuration(summary.sdevRt))
    tdStats(formatDuration(summary.minRt))
    tdStats(formatDuration(summary.p50))
    tdStats(formatDuration(summary.p90))
    tdStats(formatDuration(summary.p95))
    tdStats(formatDuration(summary.p99))
    tdStats(formatDuration(summary.maxRt))
}

/**
 * Renders a detailed table for a single benchmark.
 */
fun FlowContent.detailedBenchmarkTable(
    results: List<BenchmarkResult>, 
    tableId: String = "detailedTable"
) {
    val actions = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
    
    div(classes = "overflow-auto") {
        table(classes = "striped") {
            id = tableId
            renderTableHead("Action / Iteration")
            tbody {
                actions.forEach { actionName ->
                    val actionResults = results.mapNotNull { it.userActions.values.find { a -> a.name == actionName } }
                    val totalDuration = results.sumOf { it.duration }
                    val actionSummary = aggregateActionResults(actionResults, totalDuration)
                    
                    tr {
                        td { b { +"Summary: $actionName" } }
                        renderStatsTDs(actionSummary)
                    }

                    results.forEach { res ->
                        val stats = res.userActions.values.find { it.name == actionName }
                        if (stats != null) { renderIterationRow(res, stats) }
                    }
                }
                
                val summary = aggregateResults(results)
                tr {
                    td { b { +"OVERALL BENCHMARK" } }
                    renderStatsTDs(summary)
                }
            }
        }
    }
}

private fun TBODY.renderIterationRow(res: BenchmarkResult, stats: ActionStatResult) {
    tr {
        td { 
            div {
                style = "padding-left: ${ReportConstants.ACTION_INDENT_PX}px;"
                +"Iteration ${res.rowId + 1}"
            }
        }
        renderIterationStats(res, stats)
    }
}

private fun TR.renderIterationStats(res: BenchmarkResult, stats: ActionStatResult) {
    val s = "opacity: 0.5;"
    fun tdM(v: String) = td(classes = "numeric") { style = s; +v }

    tdM((res.rowId + 1).toString())
    tdM(stats.numActions.toString())
    td(classes = "numeric") { statusPill(stats.numFailed.toLong()) }
    tdM(formatTime(res.duration))
    tdM("%.1f".format(stats.avgAps))
    tdM(formatDuration(stats.avgRt))
    tdM(formatDuration(stats.sdevRt))
    tdM(formatDuration(stats.minRt))
    
    tdM(formatDuration(stats.percentilesRt[ReportConstants.P50.toString()] ?: 0.0))
    tdM(formatDuration(stats.percentilesRt[ReportConstants.P90.toString()] ?: 0.0))
    tdM(formatDuration(stats.percentilesRt[ReportConstants.P95.toString()] ?: 0.0))
    tdM(formatDuration(stats.percentilesRt[ReportConstants.P99.toString()] ?: 0.0))
    
    tdM(formatDuration(stats.maxRt))
}

/**
 * Renders a percentile table using Log-Linear Quantization buckets.
 */
fun FlowContent.llqPercentileTable(results: List<BenchmarkResult>, tableId: String) {
    val lastRes = results.last()
    div(classes = "overflow-auto") {
        style = "max-height: 400px;"
        table(classes = "striped") {
            id = tableId
            thead {
                tr {
                    th { +"Value" }; th { +"Percentile" }; th { +"Total Count" }
                    th { +"Bucket Size" }; th { +"Percentage" }; th { +"Above Count" }
                }
            }
            tbody {
                val totalCount = lastRes.numActions.toLong()
                ReportConstants.LLQ_POINTS.forEach { p ->
                    val valNanos = if (p == ReportConstants.P100) {
                        lastRes.maxRt
                    } else lastRes.percentilesRt[p.toString()] ?: 0.0
                    val countAtP = (totalCount * (p / ReportConstants.P100)).toLong()
                    tr {
                        td(classes = "numeric") { +formatDuration(valNanos) }
                        td(classes = "numeric") { +"%.6f".format(p / ReportConstants.P100) }
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

/**
 * Renders a percentile table using HDR Histogram data.
 */
fun FlowContent.hdrPercentileTable(results: List<BenchmarkResult>, tableId: String) {
    val lastRes = results.last()
    val h = decodeHistogram(lastRes.hdrHistogramRt) ?: return

    div(classes = "overflow-auto") {
        style = "max-height: 400px;"
        table(classes = "striped") {
            id = tableId
            thead {
                tr {
                    th { +"Value" }; th { +"Percentile" }; th { +"TotalCount" }
                    th { +"1/(1-Percentile)" }; th { +"AboveCount" }
                }
            }
            tbody {
                val data = mutableListOf<Triple<Double, Double, Long>>()
                h.percentiles(ReportConstants.HISTOGRAM_PRECISION).forEach { iv ->
                    data.add(Triple(
                        iv.percentileLevelIteratedTo, 
                        iv.valueIteratedTo.toDouble(), 
                        iv.totalCountToThisValue
                    ))
                }
                data.asReversed().forEach { (p, value, count) ->
                    val factor = if (p < ReportConstants.P100) {
                        "%.2f".format(ReportConstants.P100 / (ReportConstants.P100 - p))
                    } else ""
                    tr {
                        td(classes = "numeric") { +formatDuration(value) }
                        td(classes = "numeric") { +"%.12f".format(p / ReportConstants.P100) }
                        td(classes = "numeric") { +count.toString() }
                        td(classes = "numeric") { +factor }
                        td(classes = "numeric") { +(h.totalCount - count).toString() }
                    }
                }
            }
        }
    }
}
