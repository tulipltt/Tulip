package io.github.tulipltt.tulip.report

import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReportTablesTest {
    @Test
    fun `test summaryTable hierarchy tagging`() {
        val out = StringBuilder()
        val mockData = mapOf(
            "Benchmark 1" to listOf(
                BenchmarkResult(
                    bmName = "Benchmark 1", 
                    rowId = 0, 
                    duration = 1.0, 
                    numActions = 100,
                    userActions = mapOf("Action 1" to ActionStatResult(name = "Action 1", numActions = 100))
                )
            )
        )

        out.appendHTML().html {
            body {
                summaryTable(mockData, "test_summary")
            }
        }
        val html = out.toString()

        // Verify table class
        assertTrue(html.contains("class=\"striped sortable\""), "Table should be sortable")
        
        // Verify summary row
        assertTrue(html.contains("class=\"row-summary\""), "Summary row class missing")
        assertTrue(html.contains("data-benchmark=\"Benchmark 1\""), "Benchmark data attribute missing on summary row")
        
        // Verify action row (assuming at least one action exists or is aggregated)
        assertTrue(html.contains("class=\"row-action\""), "Action row class missing")
        assertTrue(html.contains("data-benchmark=\"Benchmark 1\""), "Benchmark data attribute missing on action row")
    }

    @Test
    fun `test detailedBenchmarkTable hierarchy tagging`() {
        val out = StringBuilder()
        val mockResults = listOf(
            BenchmarkResult(
                bmName = "Test BM", 
                rowId = 0, 
                duration = 1.0, 
                numActions = 10,
                userActions = mapOf("Action A" to ActionStatResult(name = "Action A", numActions = 10))
            )
        )

        out.appendHTML().html {
            body {
                detailedBenchmarkTable(mockResults, "test_detailed")
            }
        }
        val html = out.toString()

        // Verify summary-action row
        assertTrue(html.contains("class=\"row-summary-action\""), "Action summary row class missing")
        assertTrue(html.contains("data-action=\"Action A\""), "Action data attribute missing on summary-action row")
        
        // Verify iteration row
        assertTrue(html.contains("class=\"row-iteration\""), "Iteration row class missing")
        assertTrue(html.contains("data-action=\"Action A\""), "Action data attribute missing on iteration row")
        
        // Verify overall row
        assertTrue(html.contains("class=\"row-overall\""), "Overall row class missing")
        assertTrue(html.contains("OVERALL BENCHMARK"), "Overall label missing")
    }

    @Test
    fun `test numeric column tagging`() {
        val out = StringBuilder()
        val mockData = mapOf(
            "Benchmark 1" to listOf(
                BenchmarkResult(
                    bmName = "Benchmark 1", 
                    rowId = 0, 
                    duration = 1.0, 
                    numActions = 100,
                    userActions = mapOf("Action 1" to ActionStatResult(name = "Action 1", numActions = 100))
                )
            )
        )

        out.appendHTML().html {
            body {
                summaryTable(mockData, "test_numeric")
            }
        }
        val html = out.toString()

        // Verify numeric class is applied to data cells
        assertTrue(html.contains("class=\"numeric\""), "Numeric class missing on performance columns")
    }
}
