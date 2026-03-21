package io.github.tulipltt.tulip.report

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.File
import kotlinx.serialization.json.Json

object TulipReportGenerator {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun generateHtml(reportData: ReportData): String {
        val out = StringBuilder()
        out.append("<!DOCTYPE html>\n")
        out.appendHTML().html {
            lang = "en"
            head {
                meta { charset = "UTF-8" }
                title { +"Tulip Performance Report" }
                style {
                    unsafe { +ReportStyles.grafanaTheme }
                }
                script {
                    src = "https://www.gstatic.com/charts/loader.js"
                }
                script {
                    unsafe { +ReportScripts.googleChartsBase }
                }
            }
            body {
                h1 { +"Tulip Benchmark Report" }
                
                div("metadata-section") {
                    metadataSection("Version", reportData.version)
                    metadataSection("Timestamp", reportData.timestamp)
                    metadataSection("Java Vendor", reportData.results.firstOrNull()?.java?.javaVendor ?: "N/A")
                    metadataSection("Java Version", reportData.results.firstOrNull()?.java?.javaRuntimeVersion ?: "N/A")
                }

                div("dashboard-grid") {
                    statsCard("Benchmark Summary", classes = "full-width") {
                        summaryTable(reportData.results)
                    }

                    // Group results by benchmark name for time-series charts
                    val groupedResults = reportData.results.groupBy { it.bmName }
                    
                    groupedResults.forEach { (bmName, results) ->
                        statsCard("$bmName - APS Over Time") {
                            div("chart-container") {
                                id = "chart_aps_${bmName.replace(" ", "_")}"
                            }
                            script {
                                val dataPoints = results.map { "[${it.rowId}, ${it.avgAps}]" }.joinToString(",")
                                unsafe {
                                    +"""
                                        google.charts.setOnLoadCallback(function() {
                                            drawTimeSeriesChart('chart_aps_${bmName.replace(" ", "_")}', [$dataPoints], 'Actions Per Second', 'APS');
                                        });
                                    """.trimIndent()
                                }
                            }
                        }

                        statsCard("$bmName - Latency Over Time") {
                            div("chart-container") {
                                id = "chart_rt_${bmName.replace(" ", "_")}"
                            }
                            script {
                                val dataPoints = results.map { "[${it.rowId}, ${it.avgRt / 1_000_000.0}]" }.joinToString(",")
                                unsafe {
                                    +"""
                                        google.charts.setOnLoadCallback(function() {
                                            drawTimeSeriesChart('chart_rt_${bmName.replace(" ", "_")}', [$dataPoints], 'Average Latency', 'Latency (ms)');
                                        });
                                    """.trimIndent()
                                }
                            }
                        }
                    }
                }
            }
        }
        return out.toString()
    }

    fun createReport(jsonPath: String, htmlOutputPath: String) {
        val jsonText = File(jsonPath).readText()
        val reportData = json.decodeFromString<ReportData>(jsonText)
        val html = generateHtml(reportData)
        File(htmlOutputPath).writeText(html)
    }
}
