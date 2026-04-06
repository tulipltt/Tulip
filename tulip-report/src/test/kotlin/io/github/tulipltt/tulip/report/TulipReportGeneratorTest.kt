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

        try {
            TulipReportGenerator.createReport(actualFile.absolutePath, htmlFile.absolutePath)
        } catch (e: Exception) {
            println("EXCEPTION IN createReport: ${e.message}")
            e.printStackTrace()
            throw e
        }

        assertTrue(htmlFile.exists())
        val html = htmlFile.readText()
        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("Performance Test Results"))
        assertTrue(html.contains("All Benchmarks Summary"))
        assertTrue(html.contains("echarts.init"))
        assertTrue(html.contains("https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css"))

        val adocFile = File("build/tmp/test_report_c.adoc")
        assertTrue(adocFile.exists(), "AsciiDoc file not found")
        val adoc = adocFile.readText()
        assertTrue(adoc.contains("= Benchmark Configuration Report"))

        val adocHtmlFile = File("build/tmp/test_report_c.html")
        assertTrue(adocHtmlFile.exists(), "AsciiDoc HTML file not found")
    }
}
