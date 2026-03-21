package io.github.tulipltt.tulip.report

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.File
import kotlinx.serialization.json.Json

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode

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

    fun generateAsciiDoc(reportData: ReportData): String {
        val adoc = StringBuilder()
        adoc.append("= Benchmark Configuration Report\n")
        adoc.append(":toc: left\n\n")
        
        adoc.append("== Metadata\n\n")
        adoc.append("* *Version*: ${reportData.version}\n")
        adoc.append("* *Timestamp*: ${reportData.timestamp}\n\n")
        
        adoc.append("== Actions\n\n")
        val actions = reportData.config.actions
        if (actions != null) {
            adoc.append("[cols=\"1,2\"]\n|===\n")
            adoc.append("| Description | ${actions.description ?: ""}\n")
            adoc.append("| User Class | ${actions.userClass ?: ""}\n")
            adoc.append("| Output File | ${actions.outputFilename ?: ""}\n")
            adoc.append("| Report File | ${actions.reportFilename ?: ""}\n")
            adoc.append("|===\n\n")
            
            adoc.append("=== User Parameters\n\n")
            adoc.append("[cols=\"1,2\"]\n|===\n")
            actions.userParams.forEach { (k, v) ->
                adoc.append("| $k | $v\n")
            }
            adoc.append("|===\n\n")
        }
        
        adoc.append("== Benchmarks\n\n")
        reportData.config.benchmarks.forEach { (name, config) ->
            adoc.append("=== $name\n\n")
            adoc.append("* *Enabled*: ${config.enabled}\n")
            config.apsRate?.let { adoc.append("* *APS Rate*: $it\n") }
            config.scenarioWorkflow?.let { adoc.append("* *Workflow*: $it\n") }
            adoc.append("\n")
        }
        
        return adoc.toString()
    }

    fun convertAdocToHtml(adocPath: String) {
        val asciidoctor = Asciidoctor.Factory.create()
        val stylesheetUrl = "https://raw.githubusercontent.com/tulipltt/Tulip/refs/heads/main/docs/css/adoc-foundation.css"
        val attributes = Attributes.builder()
            .attribute("linkcss", false)
            .attribute("data-uri", true)
            .attribute("allow-uri-read", true)
            .attribute("stylesheet", stylesheetUrl)
            .build()
        
        try {
            asciidoctor.requireLibrary("asciidoctor-diagram")
        } catch (e: Exception) {
            println("Warning: asciidoctor-diagram not found")
        }

        asciidoctor.convertFile(
            File(adocPath),
            Options.builder()
                .toFile(true)
                .attributes(attributes)
                .safe(SafeMode.UNSAFE)
                .build()
        )
        asciidoctor.shutdown()
    }

    fun createReport(jsonPath: String, htmlOutputPath: String) {
        val jsonText = File(jsonPath).readText()
        val reportData = json.decodeFromString<ReportData>(jsonText)
        
        // Generate HTML Report
        val html = generateHtml(reportData)
        File(htmlOutputPath).writeText(html)
        
        // Generate AsciiDoc Report
        val adoc = generateAsciiDoc(reportData)
        val adocPath = htmlOutputPath.replace(".html", "_c.adoc")
        File(adocPath).writeText(adoc)
        
        // Convert AsciiDoc to HTML
        convertAdocToHtml(adocPath)
    }
}
