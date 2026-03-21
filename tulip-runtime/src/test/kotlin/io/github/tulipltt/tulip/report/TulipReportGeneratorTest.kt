package io.github.tulipltt.tulip.report

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class TulipReportGeneratorTest {

    @Test
    fun `test generateHtml`() {
        val jsonFile = File("../reports/report_output.json")
        val actualFile = if (jsonFile.exists()) jsonFile else File("reports/report_output.json")
        
        if (!actualFile.exists()) {
            println("Skipping test: report_output.json not found")
            return
        }

        val htmlFile = File("build/tmp/test_report.html")
        htmlFile.parentFile.mkdirs()
        
        TulipReportGenerator.createReport(actualFile.absolutePath, htmlFile.absolutePath)
        
        assertTrue(htmlFile.exists())
        val html = htmlFile.readText()
        println("GENERATED HTML: $html")
        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("Tulip Benchmark Report"))
        assertTrue(html.contains("Benchmark Summary"))
        assertTrue(html.contains("google.charts.load"))
    }
}
