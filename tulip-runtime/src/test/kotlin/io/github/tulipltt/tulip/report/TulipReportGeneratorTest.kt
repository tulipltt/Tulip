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
        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("Tulip Benchmark Report"))
        assertTrue(html.contains("Benchmark Summary"))
        assertTrue(html.contains("google.charts.load"))

        val adocFile = File("build/tmp/test_report_c.adoc")
        assertTrue(adocFile.exists(), "AsciiDoc file not found")
        val adoc = adocFile.readText()
        assertTrue(adoc.contains("= Benchmark Configuration Report"))

        val adocHtmlFile = File("build/tmp/test_report_c.html")
        assertTrue(adocHtmlFile.exists(), "AsciiDoc HTML file not found")
    }
}
