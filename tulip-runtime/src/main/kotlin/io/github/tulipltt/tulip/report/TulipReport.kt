package io.github.tulipltt.tulip.report

import java.io.File
import kotlinx.serialization.json.Json

fun createHtmlReport(outputFilename: String) {
    val jsonFile = File(outputFilename)
    if (!jsonFile.exists()) {
        println("JSON output file not found: $outputFilename")
        return
    }
    
    // Determine HTML output path from JSON config
    val jsonText = jsonFile.readText()
    val json = Json { ignoreUnknownKeys = true }
    val reportData = json.decodeFromString<ReportData>(jsonText)
    val htmlOutputPath = reportData.config.actions?.reportFilename ?: "benchmark_report.html"
    
    println("Generating HTML report: $htmlOutputPath")
    TulipReportGenerator.createReport(outputFilename, htmlOutputPath)
}
