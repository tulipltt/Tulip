package io.github.tulipltt.tulip.report

import kotlinx.html.div
import kotlinx.html.stream.createHTML
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReportChartUtilsTest {
    @Test
    fun `test renderBenchmarkCharts produces expected HTML`() {
        val results = listOf(
            BenchmarkResult(
                bmId = 1,
                bmName = "test-bm",
                userActions = mapOf(
                    "action1" to ActionStatResult(name = "Action 1", numActions = 100)
                )
            )
        )
        
        val html = createHTML().div {
            renderBenchmarkCharts("bm1", results)
        }
        
        assertTrue(html.contains("Percentile Distribution Tables"))
        assertTrue(html.contains("Benchmark Summary (Aggregated)"))
        assertTrue(html.contains("Action: Action 1"))
        assertTrue(html.contains("llq_table_bm1"))
    }
}
