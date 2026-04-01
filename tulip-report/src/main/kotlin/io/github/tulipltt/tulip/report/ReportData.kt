package io.github.tulipltt.tulip.report

import io.github.tulipltt.tulip.core.TulipConfig
import kotlinx.serialization.Serializable

@Serializable
data class ReportData(
    val version: String = "",
    val timestamp: String = "",
    val java: JavaInfo = JavaInfo(),
    val config: TulipConfig = TulipConfig(),
    val results: List<BenchmarkResult> = emptyList(),
)
