@file:Suppress("ktlint:standard:no-wildcard-imports", "WildcardImport", "TooManyFunctions")

package io.github.tulipltt.tulip.report

import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.HdrHistogram.Histogram

/**
 * Renders combined charts for all benchmarks.
 */
fun FlowContent.renderCombinedCharts(groupedResults: Map<String, List<BenchmarkResult>>) {
    renderCombinedApsChart(groupedResults)
    renderCombinedRtChart(groupedResults)
    renderCombinedDistChart(groupedResults)
}

private fun FlowContent.renderCombinedApsChart(groupedResults: Map<String, List<BenchmarkResult>>) {
    val maxRows = groupedResults.values.maxOfOrNull { it.size } ?: 0
    statsCard(
        StatsCardConfig(
            titleText = "Throughput Comparison",
            classes = "full-width",
            isChart = true,
        ),
    ) {
        div("chart-container") { id = "chart_combined_aps" }
        script {
            val seriesLabels = getCombinedApsLabels(groupedResults)
            val dataRows = getCombinedApsDataRows(maxRows, groupedResults)

            renderChartScript(
                ChartConfig(
                    type = "TimeSeries",
                    id = "chart_combined_aps",
                    labels = seriesLabels,
                    data = dataRows,
                    title = "Throughput per Action (All Benchmarks)",
                    unit = "APS",
                ),
            )
        }
    }
}

private fun getCombinedApsLabels(groupedResults: Map<String, List<BenchmarkResult>>): List<String> {
    val seriesLabels = mutableListOf<String>()
    groupedResults.forEach { (bmName, results) ->
        val actions =
            results.flatMap {
                it.userActions?.values?.map { a -> a.name ?: "" } ?: emptyList()
            }.distinct().sorted()
        actions.forEach { actionName ->
            seriesLabels.add("$bmName - $actionName")
            seriesLabels.add("$bmName - $actionName (Errors)")
        }
    }
    return seriesLabels
}

private fun getCombinedApsDataRows(
    maxRows: Int,
    groupedResults: Map<String, List<BenchmarkResult>>,
): List<List<Any?>> {
    return (0 until maxRows).map { rowIdx ->
        val rowData = mutableListOf<Any?>()
        var timestamp: String? = null
        groupedResults.forEach { (_, results) ->
            val res = results.getOrNull(rowIdx)
            if (timestamp == null && res?.testBegin != null) {
                timestamp = res.testBegin
            }
            val actions =
                results.flatMap {
                    it.userActions?.values?.map { a -> a.name ?: "" } ?: emptyList()
                }.distinct().sorted()
            actions.forEach { actionName ->
                val stats = res?.userActions?.values?.find { it.name == actionName }
                val aps = stats?.avgAps ?: 0.0
                val errAps =
                    if (stats != null && (res.duration ?: 0.0) > 0.0) {
                        (stats.numFailed ?: 0).toDouble() / (res.duration ?: 1.0)
                    } else {
                        0.0
                    }
                rowData.add(aps)
                rowData.add(errAps)
            }
        }
        listOf(timestamp ?: (rowIdx + 1).toString()) + rowData
    }
}

private fun FlowContent.renderCombinedRtChart(groupedResults: Map<String, List<BenchmarkResult>>) {
    val maxRows = groupedResults.values.maxOfOrNull { it.size } ?: 0
    statsCard(
        StatsCardConfig(
            titleText = "Latency Comparison",
            classes = "full-width",
            isChart = true,
        ),
    ) {
        div("chart-container") { id = "chart_combined_rt" }
        script {
            val seriesLabels = getCombinedRtLabels(groupedResults)
            val dataRows = getCombinedRtDataRows(maxRows, groupedResults)

            renderChartScript(
                ChartConfig(
                    type = "TimeSeries",
                    id = "chart_combined_rt",
                    labels = seriesLabels,
                    data = dataRows,
                    title = "Average Latency per Action (All Benchmarks)",
                    unit = "ms",
                ),
            )
        }
    }
}

private fun getCombinedRtLabels(groupedResults: Map<String, List<BenchmarkResult>>): List<String> {
    val seriesLabels = mutableListOf<String>()
    groupedResults.forEach { (bmName, results) ->
        val actions =
            results.flatMap {
                it.userActions?.values?.map { a -> a.name ?: "" } ?: emptyList()
            }.distinct().sorted()
        actions.forEach { actionName -> seriesLabels.add("$bmName - $actionName") }
    }
    return seriesLabels
}

private fun getCombinedRtDataRows(
    maxRows: Int,
    groupedResults: Map<String, List<BenchmarkResult>>,
): List<List<Any?>> {
    return (0 until maxRows).map { rowIdx ->
        val rowData = mutableListOf<Any?>()
        var timestamp: String? = null
        groupedResults.forEach { (_, results) ->
            val res = results.getOrNull(rowIdx)
            if (timestamp == null && res?.testBegin != null) {
                timestamp = res.testBegin
            }
            val actions =
                results.flatMap {
                    it.userActions?.values?.map { a -> a.name ?: "" } ?: emptyList()
                }.distinct().sorted()
            actions.forEach { actionName ->
                val stats = res?.userActions?.values?.find { it.name == actionName }
                val rt =
                    if (stats != null) {
                        (stats.avgRt ?: 0.0) / ReportConstants.NANOS_PER_MILLI
                    } else {
                        null
                    }
                rowData.add(rt)
            }
        }
        listOf(timestamp ?: (rowIdx + 1).toString()) + rowData
    }
}

private fun FlowContent.renderCombinedDistChart(groupedResults: Map<String, List<BenchmarkResult>>) {
    statsCard(
        StatsCardConfig(
            titleText = "Latency Percentile Distribution Comparison",
            classes = "full-width",
            isChart = true,
        ),
    ) {
        div("chart-container") { id = "chart_combined_dist" }
        script {
            val (actionNamesList, actionHistos) = getCombinedDistHistograms(groupedResults)
            val dataRows = getCombinedDistDataRows(actionHistos)

            renderChartScript(
                ChartConfig(
                    type = "Percentile",
                    id = "chart_combined_dist",
                    labels = actionNamesList,
                    data = dataRows,
                    title = "Latency Percentile Distribution (All Actions)",
                    unit = "ms",
                ),
            )
        }
    }
}

private fun getCombinedDistHistograms(
    groupedResults: Map<String, List<BenchmarkResult>>,
): Pair<List<String>, List<Histogram>> {
    val actionNamesList = mutableListOf<String>()
    val actionHistos = mutableListOf<Histogram>()
    groupedResults.forEach { (bmName, results) ->
        val lastRes = results.last()
        val names =
            results.flatMap {
                it.userActions?.values?.map { a -> a.name ?: "" } ?: emptyList()
            }.distinct().sorted()
        names.forEach { actionName ->
            actionNamesList.add("$bmName - $actionName")
            val stats = lastRes.userActions?.values?.find { it.name == actionName }
            actionHistos.add(
                decodeHistogram(stats?.hdrHistogramRt)
                    ?: Histogram(ReportConstants.HISTOGRAM_PRECISION),
            )
        }
    }
    return actionNamesList to actionHistos
}

private fun getCombinedDistDataRows(actionHistos: List<Histogram>): List<List<Double>> {
    val commonPoints = mutableSetOf<Double>()
    actionHistos.forEach { h ->
        h.percentiles(ReportConstants.HISTOGRAM_PRECISION).forEach {
            commonPoints.add(it.percentileLevelIteratedTo)
        }
    }
    val sortedPoints =
        commonPoints.toList()
            .filter { it <= ReportConstants.P100 }
            .sorted()
    return sortedPoints.map { p ->
        val x =
            if (p < ReportConstants.P100) {
                ReportConstants.P100 / (ReportConstants.P100 - p)
            } else {
                ReportConstants.PERCENTILE_CHART_MAX_X
            }
        val rowData =
            actionHistos.map { h ->
                (h.getValueAtPercentile(p) / ReportConstants.NANOS_PER_MILLI)
            }
        listOf(x) + rowData
    }
}

/**
 * Renders charts for a specific benchmark.
 */
fun FlowContent.renderBenchmarkCharts(
    bmId: String,
    results: List<BenchmarkResult>,
) {
    renderBenchmarkApsChart(bmId, results)
    renderBenchmarkRtChart(bmId, results)

    details {
        summary { +"Latency per Action" }
        renderActionLatencyCharts(bmId, results)
    }

    renderBenchmarkDistChart(bmId, results)

    details {
        summary { +"Percentile Distribution Tables" }
        renderAggregatedSummary(bmId, results.last())
        renderPerActionSummaries(bmId, results)
    }
}

private fun FlowContent.renderActionLatencyCharts(
    bmId: String,
    results: List<BenchmarkResult>,
) {
    val actions =
        results.flatMap {
            it.userActions?.values?.map { a -> a.name ?: "" } ?: emptyList()
        }.distinct().sorted()

    actions.forEach { actionName ->
        val actionId = actionName.replace(" ", "_")
        statsCard(
            StatsCardConfig(
                titleText = "Latency per Action: $actionName",
                classes = "full-width",
                isChart = true,
            ),
        ) {
            div("chart-container") { id = "chart_action_latency_${bmId}_$actionId" }
            script {
                val dataRows =
                    results.map { res ->
                        val stats = res.userActions?.values?.find { it.name == actionName }
                        val p = stats?.percentilesRt ?: emptyMap()
                        listOf(
                            res.testBegin ?: ((res.rowId ?: 0) + 1).toString(),
                            (stats?.minRt ?: 0.0) / ReportConstants.NANOS_PER_MILLI,
                            (stats?.avgRt ?: 0.0) / ReportConstants.NANOS_PER_MILLI,
                            (p["50.0"] ?: 0.0) / ReportConstants.NANOS_PER_MILLI,
                            (p["90.0"] ?: 0.0) / ReportConstants.NANOS_PER_MILLI,
                            (p["95.0"] ?: 0.0) / ReportConstants.NANOS_PER_MILLI,
                            (p["99.0"] ?: 0.0) / ReportConstants.NANOS_PER_MILLI,
                            (stats?.maxRt ?: 0.0) / ReportConstants.NANOS_PER_MILLI,
                        )
                    }
                renderChartScript(
                    ChartConfig(
                        type = "TimeSeries",
                        id = "chart_action_latency_${bmId}_$actionId",
                        labels = listOf("Min", "Avg", "P50", "P90", "P95", "P99", "Max"),
                        data = dataRows,
                        title = "Latency Over Time: $actionName",
                        unit = "ms",
                    ),
                )
            }
        }
    }
}

private fun FlowContent.renderAggregatedSummary(bmId: String, lastRes: BenchmarkResult) {
    details {
        style = "margin-top: 1rem;"
        summary { +"Benchmark Summary (Aggregated)" }
        div(classes = "grid") {
            div {
                statsCard(
                    StatsCardConfig(
                        titleText = "Log-Linear Quantization (LLQ)",
                        tableId = "llq_table_$bmId",
                    ),
                ) {
                    llqPercentileTable(
                        lastRes.percentilesRt,
                        lastRes.maxRt,
                        lastRes.numActions?.toLong() ?: 0L,
                        "llq_table_$bmId",
                    )
                }
            }
            div {
                statsCard(
                    StatsCardConfig(
                        titleText = "HDR Histogram",
                        tableId = "hdr_table_$bmId",
                    ),
                ) {
                    hdrPercentileTable(lastRes.hdrHistogramRt, "hdr_table_$bmId")
                }
            }
        }
    }
}

private fun FlowContent.renderPerActionSummaries(bmId: String, results: List<BenchmarkResult>) {
    val actions =
        results.flatMap {
            it.userActions?.values?.map { a -> a.name ?: "" } ?: emptyList()
        }.distinct().sorted()

    actions.forEach { actionName ->
        val actionId = actionName.replace(" ", "_")
        details {
            style = "margin-top: 0.5rem;"
            summary { +"Action: $actionName" }
            val lastRes = results.last()
            val stats = lastRes.userActions?.values?.find { it.name == actionName }

            div(classes = "grid") {
                div {
                    statsCard(
                        StatsCardConfig(
                            titleText = "LLQ: $actionName",
                            tableId = "llq_table_${bmId}_$actionId",
                        ),
                    ) {
                        llqPercentileTable(
                            stats?.percentilesRt,
                            stats?.maxRt,
                            stats?.numActions?.toLong() ?: 0L,
                            "llq_table_${bmId}_$actionId",
                        )
                    }
                }
                div {
                    statsCard(
                        StatsCardConfig(
                            titleText = "HDR: $actionName",
                            tableId = "hdr_table_${bmId}_$actionId",
                        ),
                    ) {
                        hdrPercentileTable(stats?.hdrHistogramRt, "hdr_table_${bmId}_$actionId")
                    }
                }
            }
        }
    }
}

private fun FlowContent.renderBenchmarkApsChart(
    bmId: String,
    results: List<BenchmarkResult>,
) {
    statsCard(
        StatsCardConfig(
            titleText = "Throughput (APS) per Action",
            classes = "full-width",
            isChart = true,
        ),
    ) {
        div("chart-container") { id = "chart_aps_$bmId" }
        script {
            val actions =
                results.flatMap {
                    it.userActions?.values?.map { a -> a.name ?: "" } ?: emptyList()
                }.distinct().sorted()
            val seriesLabels = actions.flatMap { listOf(it, "$it (Errors)") }
            val dataRows =
                results.map { res ->
                    val rowData =
                        actions.flatMap { actionName ->
                            val stats = res.userActions?.values?.find { it.name == actionName }
                            val aps = stats?.avgAps ?: 0.0
                            val errAps =
                                if (stats != null && (res.duration ?: 0.0) > 0.0) {
                                    (stats.numFailed ?: 0).toDouble() / (res.duration ?: 1.0)
                                } else {
                                    0.0
                                }
                            listOf(aps, errAps)
                        }
                    listOf<Any?>(res.testBegin ?: ((res.rowId ?: 0) + 1).toString()) + rowData
                }

            renderChartScript(
                ChartConfig(
                    type = "TimeSeries",
                    id = "chart_aps_$bmId",
                    labels = seriesLabels,
                    data = dataRows,
                    title = "Throughput per Action",
                    unit = "APS",
                ),
            )
        }
    }
}

private fun FlowContent.renderBenchmarkRtChart(
    bmId: String,
    results: List<BenchmarkResult>,
) {
    statsCard(
        StatsCardConfig(
            titleText = "Average Latency per Action",
            classes = "full-width",
            isChart = true,
        ),
    ) {
        div("chart-container") { id = "chart_rt_$bmId" }
        script {
            val actions =
                results.flatMap {
                    it.userActions?.values?.map { a -> a.name ?: "" } ?: emptyList()
                }.distinct().sorted()
            val dataRows =
                results.map { res ->
                    val rowData =
                        actions.map { actionName ->
                            val stats = res.userActions?.values?.find { it.name == actionName }
                            if (stats != null) {
                                (stats.avgRt ?: 0.0) / ReportConstants.NANOS_PER_MILLI
                            } else {
                                null
                            }
                        }
                    listOf<Any?>(res.testBegin ?: ((res.rowId ?: 0) + 1).toString()) + rowData
                }

            renderChartScript(
                ChartConfig(
                    type = "TimeSeries",
                    id = "chart_rt_$bmId",
                    labels = actions,
                    data = dataRows,
                    title = "Avg Latency per Action (ms)",
                    unit = "ms",
                ),
            )
        }
    }
}

private fun FlowContent.renderBenchmarkDistChart(
    bmId: String,
    results: List<BenchmarkResult>,
) {
    statsCard(
        StatsCardConfig(
            titleText = "Latency Percentile Distribution per Action",
            classes = "full-width",
            isChart = true,
        ),
    ) {
        div("chart-container") { id = "chart_dist_$bmId" }
        script {
            val lastRes = results.last()
            val actions =
                results.flatMap {
                    it.userActions?.values?.map { a -> a.name ?: "" } ?: emptyList()
                }.distinct().sorted()
            val actionHistos =
                actions.map { actionName ->
                    val stats = lastRes.userActions?.values?.find { it.name == actionName }
                    decodeHistogram(stats?.hdrHistogramRt)
                        ?: Histogram(ReportConstants.HISTOGRAM_PRECISION)
                }
            val commonPoints = mutableSetOf<Double>()
            actionHistos.forEach { h ->
                h.percentiles(ReportConstants.HISTOGRAM_PRECISION).forEach {
                    commonPoints.add(it.percentileLevelIteratedTo)
                }
            }
            val sortedPoints =
        commonPoints.toList()
            .filter { it <= ReportConstants.P100 }
            .sorted()
            val dataRows =
                sortedPoints.map { p ->
                    val x =
                        if (p < ReportConstants.P100) {
                            ReportConstants.P100 / (ReportConstants.P100 - p)
                        } else {
                            ReportConstants.PERCENTILE_CHART_MAX_X
                        }
                    val rowData =
                        actionHistos.map { h ->
                            (h.getValueAtPercentile(p) / ReportConstants.NANOS_PER_MILLI)
                        }
                    listOf(x) + rowData
                }

            renderChartScript(
                ChartConfig(
                    type = "Percentile",
                    id = "chart_dist_$bmId",
                    labels = actions,
                    data = dataRows,
                    title = "Latency Percentile Distribution (ms)",
                    unit = "ms",
                ),
            )
        }
    }
}

private fun SCRIPT.renderChartScript(config: ChartConfig) {
    val labelsJson = Json.encodeToString(config.labels)
    
    // Manually build JsonArray to handle heterogeneous types (String, Double, null)
    val dataJson = buildJsonArray {
        config.data.forEach { row ->
            add(buildJsonArray {
                row.forEach { cell ->
                    when (cell) {
                        is String -> add(cell)
                        is Number -> add(cell)
                        null -> add(JsonNull)
                        else -> add(cell.toString())
                    }
                }
            })
        }
    }.toString()

    unsafe {
        +"create${config.type}Chart('${config.id}', $labelsJson, $dataJson, '${config.title}', '${config.unit}');"
    }
}

