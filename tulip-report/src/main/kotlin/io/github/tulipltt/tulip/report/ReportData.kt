package io.github.tulipltt.tulip.report

import io.github.tulipltt.tulip.core.TulipConfig
import kotlinx.serialization.Serializable

/**
 * Top-level container for all benchmark results and configuration.
 * This is the root object deserialized from the benchmark output JSON.
 */
@Serializable
data class ReportData(
    val version: String = "",
    val timestamp: String = "",
    val java: JavaInfo = JavaInfo(),
    val config: TulipConfig = TulipConfig(),
    val results: List<BenchmarkResult> = emptyList(),
)
