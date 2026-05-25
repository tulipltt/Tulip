package io.github.tulipltt.tulip.report

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReportDataValidatorTest {
    @Test
    fun `test validation with missing results`() {
        val data = ReportData(results = emptyList())
        val warnings = ReportDataValidator.validate(data)
        assertTrue(warnings.any { it.contains("No benchmark results found") })
    }

    @Test
    fun `test validation with missing benchmark name`() {
        val result = BenchmarkResult(bmName = null, duration = 10.0, numActions = 100)
        val data = ReportData(results = listOf(result))
        val warnings = ReportDataValidator.validate(data)
        assertTrue(warnings.any { it.contains("missing a name") })
    }

    @Test
    fun `test validation with invalid duration`() {
        val result = BenchmarkResult(bmName = "Test BM", duration = -5.0, numActions = 100)
        val data = ReportData(results = listOf(result))
        val warnings = ReportDataValidator.validate(data)
        assertTrue(warnings.any { it.contains("has invalid duration") })
    }

    @Test
    fun `test validation with missing action count`() {
        val result = BenchmarkResult(bmName = "Test BM", duration = 10.0, numActions = null)
        val data = ReportData(results = listOf(result))
        val warnings = ReportDataValidator.validate(data)
        assertTrue(warnings.any { it.contains("missing action count") })
    }
}
