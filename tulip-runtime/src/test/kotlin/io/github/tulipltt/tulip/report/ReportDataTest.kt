package io.github.tulipltt.tulip.report

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.File

class ReportDataTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `test parsing report_output json`() {
        val jsonFile = File("../reports/report_output.json")
        // If running from project root, path might be different. 
        // Let's try multiple common locations for development.
        val actualFile = if (jsonFile.exists()) {
            jsonFile
        } else {
            File("reports/report_output.json")
        }

        assertNotNull(actualFile.exists(), "JSON file not found at ${actualFile.absolutePath}")
        
        val jsonText = actualFile.readText()
        val reportData = try {
            json.decodeFromString<ReportData>(jsonText)
        } catch (e: Exception) {
            println("ERROR PARSING JSON: ${e.message}")
            throw e
        }

        assertNotNull(reportData)
        assertEquals("2.2.0", reportData.version)
        assertNotNull(reportData.config)
        assertNotNull(reportData.results)
        assertEquals(18, reportData.results.size)
        
        val firstResult = reportData.results[0]
        assertEquals("Context-1", firstResult.contextName)
        assertEquals("Constant TPS", firstResult.bmName)
        assertNotNull(firstResult.java)
        assertEquals("Eclipse Adoptium", firstResult.java?.javaVendor)
    }
}
