package io.github.tulipltt.tulip.report

import io.github.tulipltt.tulip.core.TulipConfig
import kotlinx.html.*

/**
 * A standard card component for displaying statistics, charts, or tables.
 */
fun FlowContent.statsCard(
    titleText: String,
    classes: String? = null,
    isChart: Boolean = false,
    tableId: String? = null,
    block: DIV.() -> Unit
) {
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
                if (tableId != null) {
                    tableControls(titleText, tableId)
                }
            }
        }
        div {
            block()
        }
    }
}

/**
 * Renders the control buttons for a table (maximize, export).
 */
private fun FlowContent.tableControls(titleText: String, tableId: String) {
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

/**
 * Formats a duration in seconds into a human-readable string.
 */
fun formatTime(seconds: Double): String = "%.1f s".format(seconds)

/**
 * Renders the configuration section of the report.
 */
fun FlowContent.configSection(config: TulipConfig) {
    statsCard(titleText = "General Config", classes = "full-width") {
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
        renderMapTable(actions.userParams)

        h5 { b { +"User Actions" } }
        renderMapTable(actions.userActions)
    }

    if (config.contexts.isNotEmpty()) renderContexts(config)
    renderBenchmarkConfigs(config)
    if (config.workflows.isNotEmpty()) renderWorkflows(config)
}

private fun FlowContent.renderContexts(config: TulipConfig) {
    statsCard(titleText = "Contexts", classes = "full-width") {
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

private fun FlowContent.renderBenchmarkConfigs(config: TulipConfig) {
    statsCard(titleText = "Benchmark Configurations", classes = "full-width") {
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
}

private fun FlowContent.renderWorkflows(config: TulipConfig) {
    statsCard(titleText = "Workflows", classes = "full-width") {
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

private fun FlowContent.renderMapTable(map: Map<out Any, Any>) {
    div(classes = "overflow-auto") {
        table {
            thead { tr { th { +"Key" }; th { +"Value" } } }
            tbody {
                map.forEach { (k, v) ->
                    tr { td { +k.toString() }; td { +v.toString() } }
                }
            }
        }
    }
}

/**
 * Renders the runtime information section.
 */
fun FlowContent.runtimeSection(reportData: ReportData) {
    statsCard(titleText = "Tulip Runtime Information", classes = "full-width") {
        val java = reportData.java
        
        h5 { b { +"Java System Properties" } }
        renderMapTable(java.systemProperties)

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

/**
 * Formats a duration in nanoseconds into a human-readable string.
 */
fun formatDuration(nanos: Double): String {
    return when {
        nanos < ReportConstants.NANOS_PER_MICRO -> "%.1f ns".format(nanos)
        nanos < ReportConstants.NANOS_PER_MILLI -> "%.1f µs".format(nanos / ReportConstants.NANOS_PER_MICRO)
        nanos < ReportConstants.NANOS_PER_SEC -> "%.1f ms".format(nanos / ReportConstants.NANOS_PER_MILLI)
        else -> "%.2f s".format(nanos / ReportConstants.NANOS_PER_SEC)
    }
}
