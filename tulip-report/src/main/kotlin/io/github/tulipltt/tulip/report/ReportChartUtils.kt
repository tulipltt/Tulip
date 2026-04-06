package io.github.tulipltt.tulip.report

import kotlinx.html.*
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
    statsCard("Throughput Comparison", classes = "full-width", isChart = true) {
        div("chart-container") { id = "chart_combined_aps" }
        script {
            val seriesLabels = mutableListOf<String>()
            groupedResults.forEach { (bmName, results) ->
                val actions = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                actions.forEach { actionName ->
                    seriesLabels.add("'$bmName - $actionName'")
                    seriesLabels.add("'$bmName - $actionName (Errors)'")
                }
            }
            val labelsStr = seriesLabels.joinToString(",")
            val dataRows = (0 until maxRows).map { rowIdx ->
                val rowData = mutableListOf<String>()
                groupedResults.forEach { (_, results) ->
                    val res = results.getOrNull(rowIdx)
                    val actions = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                    actions.forEach { actionName ->
                        val stats = res?.userActions?.values?.find { it.name == actionName }
                        val aps = stats?.avgAps ?: 0.0
                        val errAps = if (stats != null && res.duration > 0) {
                            stats.numFailed.toDouble() / res.duration
                        } else 0.0
                        rowData.add(aps.toString())
                        rowData.add(errAps.toString())
                    }
                }
                "[$rowIdx, ${rowData.joinToString(",")}]"
            }.joinToString(",")
            val cfg = ReportChartConfig(
                id = "chart_combined_aps", labels = labelsStr, data = dataRows,
                title = "Throughput per Action (All Benchmarks)", unit = "APS", type = "TimeSeries"
            )
            renderChartScript(cfg)
        }
    }
}

private fun FlowContent.renderCombinedRtChart(groupedResults: Map<String, List<BenchmarkResult>>) {
    val maxRows = groupedResults.values.maxOfOrNull { it.size } ?: 0
    statsCard("Latency Comparison", classes = "full-width", isChart = true) {
        div("chart-container") { id = "chart_combined_rt" }
        script {
            val seriesLabels = mutableListOf<String>()
            groupedResults.forEach { (bmName, results) ->
                val actions = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                actions.forEach { actionName -> seriesLabels.add("'$bmName - $actionName'") }
            }
            val labelsStr = seriesLabels.joinToString(",")
            val dataRows = (0 until maxRows).map { rowIdx ->
                val rowData = mutableListOf<String>()
                groupedResults.forEach { (_, results) ->
                    val res = results.getOrNull(rowIdx)
                    val actions = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                    actions.forEach { actionName ->
                        val stats = res?.userActions?.values?.find { it.name == actionName }
                        val rt = if (stats != null) (stats.avgRt / ReportConstants.NANOS_PER_MILLI) else "null"
                        rowData.add(rt.toString())
                    }
                }
                "[$rowIdx, ${rowData.joinToString(",")}]"
            }.joinToString(",")
            val cfg = ReportChartConfig(
                id = "chart_combined_rt", labels = labelsStr, data = dataRows,
                title = "Average Latency per Action (All Benchmarks)", unit = "ms", type = "TimeSeries"
            )
            renderChartScript(cfg)
        }
    }
}

private fun FlowContent.renderCombinedDistChart(groupedResults: Map<String, List<BenchmarkResult>>) {
    statsCard("Tail Latency Comparison", classes = "full-width", isChart = true) {
        div("chart-container") { id = "chart_combined_dist" }
        script {
            val actionNamesList = mutableListOf<String>()
            val actionHistos = mutableListOf<Histogram>()
            groupedResults.forEach { (bmName, results) ->
                val lastRes = results.last()
                val names = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                names.forEach { actionName ->
                    actionNamesList.add("$bmName - $actionName")
                    val stats = lastRes.userActions.values.find { it.name == actionName }
                    actionHistos.add(
                        decodeHistogram(stats?.hdrHistogramRt) ?: Histogram(ReportConstants.HISTOGRAM_PRECISION)
                    )
                }
            }
            val labelsStr = actionNamesList.joinToString(",") { "'$it'" }
            val commonPoints = mutableSetOf<Double>()
            actionHistos.forEach { h -> 
                h.percentiles(ReportConstants.HISTOGRAM_PRECISION).forEach { commonPoints.add(it.percentileLevelIteratedTo) } 
            }
            val sortedPoints = commonPoints.toList().sorted()
            val dataRows = sortedPoints.joinToString(",") { p ->
                val x = if (p < ReportConstants.P100) {
                    ReportConstants.P100 / (ReportConstants.P100 - p)
                } else ReportConstants.PERCENTILE_CHART_MAX_X
                val rowData = actionHistos.joinToString(",") { h ->
                    (h.getValueAtPercentile(p) / ReportConstants.NANOS_PER_MILLI).toString()
                }
                "[$x, $rowData]"
            }
            val cfg = ReportChartConfig(
                id = "chart_combined_dist", labels = labelsStr, data = dataRows,
                title = "Tail Latency Comparison (All Actions)", unit = "ms", type = "Percentile"
            )
            renderChartScript(cfg)
        }
    }
}

/**
 * Renders charts for a specific benchmark.
 */
fun FlowContent.renderBenchmarkCharts(bmId: String, results: List<BenchmarkResult>) {
    renderBenchmarkApsChart(bmId, results)
    renderBenchmarkRtChart(bmId, results)
    renderBenchmarkDistChart(bmId, results)
}

private fun FlowContent.renderBenchmarkApsChart(bmId: String, results: List<BenchmarkResult>) {
    statsCard("Throughput (APS) per Action", classes = "full-width", isChart = true) {
        div("chart-container") { id = "chart_aps_$bmId" }
        script {
            val actions = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
            val labelsStr = actions.flatMap { listOf("'$it'", "'$it (Errors)'") }.joinToString(",")
            val dataRows = results.map { res ->
                val rowData = actions.map { actionName ->
                    val stats = res.userActions.values.find { it.name == actionName }
                    val aps = stats?.avgAps ?: 0.0
                    val errAps = if (stats != null && res.duration > 0) {
                        stats.numFailed.toDouble() / res.duration
                    } else 0.0
                    "$aps, $errAps"
                }.joinToString(",")
                "[${res.rowId}, $rowData]"
            }.joinToString(",")
            val cfg = ReportChartConfig(
                id = "chart_aps_$bmId", labels = labelsStr, data = dataRows,
                title = "Throughput per Action", unit = "APS", type = "TimeSeries"
            )
            renderChartScript(cfg)
        }
    }
}

private fun FlowContent.renderBenchmarkRtChart(bmId: String, results: List<BenchmarkResult>) {
    statsCard("Average Latency per Action", classes = "full-width", isChart = true) {
        div("chart-container") { id = "chart_rt_$bmId" }
        script {
            val actions = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
            val labelsStr = actions.joinToString(",") { "'$it'" }
            val dataRows = results.map { res ->
                val rowData = actions.map { actionName ->
                    val stats = res.userActions.values.find { it.name == actionName }
                    val rt = if (stats != null) (stats.avgRt / ReportConstants.NANOS_PER_MILLI) else "null"
                    rt.toString()
                }.joinToString(",")
                "[${res.rowId}, $rowData]"
            }.joinToString(",")
            val cfg = ReportChartConfig(
                id = "chart_rt_$bmId", labels = labelsStr, data = dataRows,
                title = "Avg Latency per Action (ms)", unit = "ms", type = "TimeSeries"
            )
            renderChartScript(cfg)
        }
    }
}

private fun FlowContent.renderBenchmarkDistChart(bmId: String, results: List<BenchmarkResult>) {
    statsCard("Tail Latency per Action", classes = "full-width", isChart = true) {
        div("chart-container") { id = "chart_dist_$bmId" }
        script {
            val lastRes = results.last()
            val actions = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
            val labelsStr = actions.joinToString(",") { "'$it'" }
            val actionHistos = actions.map { actionName ->
                val stats = lastRes.userActions.values.find { it.name == actionName }
                decodeHistogram(stats?.hdrHistogramRt) ?: Histogram(ReportConstants.HISTOGRAM_PRECISION)
            }
            val commonPoints = mutableSetOf<Double>()
            actionHistos.forEach { h -> 
                h.percentiles(ReportConstants.HISTOGRAM_PRECISION).forEach { commonPoints.add(it.percentileLevelIteratedTo) } 
            }
            val sortedPoints = commonPoints.toList().sorted()
            val dataPoints = sortedPoints.joinToString(",") { p ->
                val x = if (p < ReportConstants.P100) {
                    ReportConstants.P100 / (ReportConstants.P100 - p)
                } else ReportConstants.PERCENTILE_CHART_MAX_X
                val rowData = actionHistos.joinToString(",") { h ->
                    (h.getValueAtPercentile(p) / ReportConstants.NANOS_PER_MILLI).toString()
                }
                "[$x, $rowData]"
            }
            val cfg = ReportChartConfig(
                id = "chart_dist_$bmId", labels = labelsStr, data = dataPoints,
                title = "Tail Latency per Action (ms)", unit = "ms", type = "Percentile"
            )
            renderChartScript(cfg)
        }
    }
}

private fun SCRIPT.renderChartScript(cfg: ReportChartConfig) {
    unsafe {
        val s = "create${cfg.type}Chart('${cfg.id}', [${cfg.labels}], [${cfg.data}], '${cfg.title}', '${cfg.unit}');"
        +s
    }
}
