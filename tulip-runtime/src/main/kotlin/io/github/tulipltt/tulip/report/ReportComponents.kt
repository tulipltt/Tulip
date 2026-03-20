package io.github.tulipltt.tulip.report

import kotlinx.html.*

fun FlowContent.statsCard(title: String, classes: String? = null, block: DIV.() -> Unit) {
    val combinedClasses = if (classes != null) "card $classes" else "card"
    div(combinedClasses) {
        div("card-header") {
            +title
        }
        block()
    }
}

fun FlowContent.metadataSection(label: String, value: String) {
    div("metadata-item") {
        span("metadata-label") { +label }
        span("metadata-value") { +value }
    }
}

fun FlowContent.summaryTable(results: List<BenchmarkResult>) {
    table("stats-table") {
        thead {
            tr {
                th { +"Benchmark" }
                th { +"Users" }
                th { +"# Actions" }
                th { +"# Failed" }
                th { +"Duration (s)" }
                th { +"Avg APS" }
                th { +"Avg RT (ms)" }
                th { +"p99 (ms)" }
            }
        }
        tbody {
            results.forEach { res ->
                tr {
                    td { +res.bmName }
                    td("numeric") { +res.numUsers.toString() }
                    td("numeric") { +res.numActions.toString() }
                    td("numeric") { 
                        if (res.numFailed > 0) {
                            attributes["class"] = "numeric status-failed"
                        }
                        +res.numFailed.toString() 
                    }
                    td("numeric") { +"%.2f".format(res.duration) }
                    td("numeric") { +"%.2f".format(res.avgAps) }
                    td("numeric") { +"%.2f".format(res.avgRt / 1_000_000.0) }
                    td("numeric") { +"%.2f".format((res.percentilesRt["99.0"] ?: 0.0) / 1_000_000.0) }
                }
            }
        }
    }
}
