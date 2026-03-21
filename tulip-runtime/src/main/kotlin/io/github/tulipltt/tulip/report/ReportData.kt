package io.github.tulipltt.tulip.report

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import kotlinx.serialization.json.JsonElement

@Serializable
data class ReportData(
    val version: String,
    val timestamp: String,
    val config: Config,
    val results: List<BenchmarkResult>
)

@Serializable
data class Config(
    val actions: ActionConfig? = null,
    val contexts: Map<String, ContextConfig> = emptyMap(),
    val benchmarks: Map<String, BenchmarkConfig> = emptyMap(),
    val workflows: Map<String, Map<String, Map<String, Double>>> = emptyMap()
)

@Serializable
data class ActionConfig(
    val description: String? = null,
    @SerialName("output_filename") val outputFilename: String? = null,
    @SerialName("report_filename") val reportFilename: String? = null,
    @SerialName("user_class") val userClass: String? = null,
    @SerialName("user_params") val userParams: Map<String, JsonElement> = emptyMap(),
    @SerialName("user_actions") val userActions: Map<String, String> = emptyMap()
)

@Serializable
data class ContextConfig(
    val enabled: Boolean = true,
    @SerialName("num_users") val numUsers: Int = 0
)

@Serializable
data class BenchmarkConfig(
    val enabled: Boolean = true,
    @SerialName("save_stats") val saveStats: Boolean = false,
    val time: TimeConfig? = null,
    @SerialName("aps_rate") val apsRate: Double? = null,
    @SerialName("aps_rate_step_change") val apsRateStepChange: Double? = null,
    @SerialName("aps_rate_step_count") val apsRateStepCount: Int? = null,
    @SerialName("scenario_workflow") val scenarioWorkflow: String? = null,
    @SerialName("scenario_actions") val scenarioActions: List<Map<String, Int>> = emptyList()
)

@Serializable
data class TimeConfig(
    @SerialName("warmup_duration1") val warmupDuration1: Int = 0,
    @SerialName("warmup_duration2") val warmupDuration2: Int = 0,
    @SerialName("benchmark_duration") val benchmarkDuration: Int = 0,
    @SerialName("benchmark_iterations") val benchmarkIterations: Int = 0
)

@Serializable
data class BenchmarkResult(
    @SerialName("context_name") val contextName: String = "",
    @SerialName("context_id") val contextId: Int = 0,
    @SerialName("bm_name") val bmName: String = "",
    @SerialName("bm_id") val bmId: Int = 0,
    @SerialName("row_id") val rowId: Int = 0,
    @SerialName("num_users") val numUsers: Int = 0,
    @SerialName("num_threads") val numThreads: Int = 0,
    @SerialName("queue_length") val queueLength: Int = 0,
    @SerialName("workflow_name") val workflowName: String = "",
    @SerialName("test_begin") val testBegin: String = "",
    @SerialName("test_end") val testEnd: String = "",
    val java: JavaInfo? = null,
    val duration: Double = 0.0,
    @SerialName("jvm_memory_used") val jvmMemoryUsed: Long = 0,
    @SerialName("jvm_memory_free") val jvmMemoryFree: Long = 0,
    @SerialName("jvm_memory_total") val jvmMemoryTotal: Long = 0,
    @SerialName("jvm_memory_maximum") val jvmMemoryMaximum: Long = 0,
    @SerialName("process_cpu_utilization") val processCpuUtilization: Double = 0.0,
    @SerialName("process_cpu_cores") val processCpuCores: Double = 0.0,
    @SerialName("process_cpu_time_ns") val processCpuTimeNs: Long = 0,
    @SerialName("avg_wthread_qsize") val avgWthreadQsize: Double = 0.0,
    @SerialName("max_wthread_qsize") val maxWthreadQsize: Int = 0,
    @SerialName("avg_wt") val avgWt: Double = 0.0,
    @SerialName("max_wt") val maxWt: Double = 0.0,
    @SerialName("num_actions") val numActions: Long = 0,
    @SerialName("num_failed") val numFailed: Long = 0,
    @SerialName("avg_aps") val avgAps: Double = 0.0,
    @SerialName("aps_target_rate") val apsTargetRate: Double = 0.0,
    @SerialName("avg_rt") val avgRt: Double = 0.0,
    @SerialName("sdev_rt") val sdevRt: Double = 0.0,
    @SerialName("min_rt") val minRt: Double = 0.0,
    @SerialName("max_rt") val maxRt: Double = 0.0,
    @SerialName("max_rt_ts") val maxRtTs: String = "",
    @SerialName("percentiles_rt") val percentilesRt: Map<String, Double> = emptyMap(),
    @SerialName("hdr_histogram_rt") val hdrHistogramRt: String? = null,
    @SerialName("user_actions") val userActions: Map<String, ActionStats> = emptyMap()
)

@Serializable
data class JavaInfo(
    @SerialName("java.vendor") val javaVendor: String,
    @SerialName("java.runtime.version") val javaRuntimeVersion: String,
    @SerialName("kotlin.version") val kotlinVersion: String
)

@Serializable
data class ActionStats(
    val name: String,
    @SerialName("num_actions") val numActions: Long = 0,
    @SerialName("num_failed") val numFailed: Long = 0,
    @SerialName("avg_aps") val avgAps: Double = 0.0,
    @SerialName("aps_target_rate") val apsTargetRate: Double = 0.0,
    @SerialName("avg_rt") val avgRt: Double = 0.0,
    @SerialName("sdev_rt") val sdevRt: Double = 0.0,
    @SerialName("min_rt") val minRt: Double = 0.0,
    @SerialName("max_rt") val maxRt: Double = 0.0,
    @SerialName("max_rt_ts") val maxRtTs: String = "",
    @SerialName("percentiles_rt") val percentilesRt: Map<String, Double> = emptyMap(),
    @SerialName("hdr_histogram_rt") val hdrHistogramRt: String? = null
)
