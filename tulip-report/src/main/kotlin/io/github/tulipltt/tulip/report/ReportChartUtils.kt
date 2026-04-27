@file:Suppress("ktlint:standard:no-wildcard-imports", "WildcardImport", "TooManyFunctions")

package io.github.tulipltt.tulip.report

import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
): List<List<Double>> {
    return (0 until maxRows).map { rowIdx ->
        val rowData = mutableListOf<Double>()
        groupedResults.forEach { (_, results) ->
            val res = results.getOrNull(rowIdx)
            val actions =
                results.flatMap {
                    it.userActions?.values?.map { a -> a.name ?: "" } ?: emptyList()
                }.distinct().sorted()
            actions.forEach { actionName ->
                val stats = res?.userActions?.values?.find { it.name == actionName }
                val aps = stats?.avgAps ?: 0.0
                val errAps =
                    if (stats != null && (res?.duration ?: 0.0) > 0.0) {
                        (stats.numFailed ?: 0).toDouble() / (res?.duration ?: 1.0)
                    } else {
                        0.0
                    }
                rowData.add(aps)
                rowData.add(errAps)
            }
        }
        listOf(rowIdx.toDouble()) + rowData
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
): List<List<Double?>> {
    return (0 until maxRows).map { rowIdx ->
        val rowData = mutableListOf<Double?>()
        groupedResults.forEach { (_, results) ->
            val res = results.getOrNull(rowIdx)
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
        listOf(rowIdx.toDouble() + 1) + rowData
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
    val sortedPoints = commonPoints.toList().sorted()
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
    renderBenchmarkDistChart(bmId, results)

    details {
        summary { +"Percentile Distribution Tables" }
        div(classes = "grid") {
            div {
                statsCard(
                    StatsCardConfig(
                        titleText = "Log-Linear Quantization (LLQ)",
                        tableId = "llq_table_$bmId",
                    ),
                ) {
                    llqPercentileTable(results, "llq_table_$bmId")
                }
            }
            div {
                statsCard(
                    StatsCardConfig(
                        titleText = "HDR Histogram",
                        tableId = "hdr_table_$bmId",
                    ),
                ) {
                    hdrPercentileTable(results, "hdr_table_$bmId")
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
                    listOf((res.rowId ?: 0).toDouble()) + rowData
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
                    listOf((res.rowId ?: 0).toDouble()) + rowData
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
            val sortedPoints = commonPoints.toList().sorted()
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
    val dataJson = Json.encodeToString(config.data)

    unsafe {
        +"create${config.type}Chart('${config.id}', $labelsJson, $dataJson, '${config.title}', '${config.unit}');"
    }
}

