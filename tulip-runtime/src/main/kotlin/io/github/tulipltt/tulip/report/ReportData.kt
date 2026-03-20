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
    @SerialName("context_name") val contextName: String,
    @SerialName("context_id") val contextId: Int,
    @SerialName("bm_name") val bmName: String,
    @SerialName("bm_id") val bmId: Int,
    @SerialName("row_id") val rowId: Int,
    @SerialName("num_users") val numUsers: Int,
    @SerialName("num_threads") val numThreads: Int,
    @SerialName("queue_length") val queueLength: Int,
    @SerialName("workflow_name") val workflowName: String,
    @SerialName("test_begin") val testBegin: String,
    @SerialName("test_end") val testEnd: String,
    val java: JavaInfo,
    val duration: Double,
    @SerialName("jvm_memory_used") val jvmMemoryUsed: Long,
    @SerialName("jvm_memory_free") val jvmMemoryFree: Long,
    @SerialName("jvm_memory_total") val jvmMemoryTotal: Long,
    @SerialName("jvm_memory_maximum") val jvmMemoryMaximum: Long,
    @SerialName("process_cpu_utilization") val processCpuUtilization: Double,
    @SerialName("process_cpu_cores") val processCpuCores: Double,
    @SerialName("process_cpu_time_ns") val processCpuTimeNs: Long,
    @SerialName("avg_wthread_qsize") val avgWthreadQsize: Double,
    @SerialName("max_wthread_qsize") val maxWthreadQsize: Int,
    @SerialName("avg_wt") val avgWt: Double,
    @SerialName("max_wt") val maxWt: Double,
    @SerialName("num_actions") val numActions: Long,
    @SerialName("num_failed") val numFailed: Long,
    @SerialName("avg_aps") val avgAps: Double,
    @SerialName("aps_target_rate") val apsTargetRate: Double,
    @SerialName("avg_rt") val avgRt: Double,
    @SerialName("sdev_rt") val sdevRt: Double,
    @SerialName("min_rt") val minRt: Double,
    @SerialName("max_rt") val maxRt: Double,
    @SerialName("max_rt_ts") val maxRtTs: String,
    @SerialName("percentiles_rt") val percentilesRt: Map<String, Double>,
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
    @SerialName("num_actions") val numActions: Long,
    @SerialName("num_failed") val numFailed: Long,
    @SerialName("avg_aps") val avgAps: Double,
    @SerialName("aps_target_rate") val apsTargetRate: Double,
    @SerialName("avg_rt") val avgRt: Double,
    @SerialName("sdev_rt") val sdevRt: Double,
    @SerialName("min_rt") val minRt: Double,
    @SerialName("max_rt") val maxRt: Double,
    @SerialName("max_rt_ts") val maxRtTs: String,
    @SerialName("percentiles_rt") val percentilesRt: Map<String, Double>,
    @SerialName("hdr_histogram_rt") val hdrHistogramRt: String? = null
)
