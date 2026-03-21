package io.github.tulipltt.tulip.report

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.GZIPInputStream
import kotlinx.serialization.json.Json
import org.python.util.PythonInterpreter

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

@Throws(IOException::class)
fun decompress(compressedData: ByteArray): String? {
    val bis = ByteArrayInputStream(compressedData)
    val gzip = GZIPInputStream(bis)
    val bos = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var len: Int
    while ((gzip.read(buffer).also { len = it }) != -1) {
        bos.write(buffer, 0, len)
    }
    gzip.close()
    bis.close()
    bos.close()
    return bos.toString(StandardCharsets.UTF_8.name())
}
