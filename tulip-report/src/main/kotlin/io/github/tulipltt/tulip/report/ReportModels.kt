package io.github.tulipltt.tulip.report

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Information about the Java Runtime Environment used during the benchmark.
 */
@Serializable
data class JavaInfo(
    @SerialName("jvm.system.properties") val systemProperties: Map<String, String>? = null,
    @SerialName("jvm.runtime.options") val runtimeOptions: List<String>? = null,
)

/**
 * Statistics for a specific user action within a benchmark iteration.
 */
@Serializable
data class ActionStatResult(
    val name: String? = null,
    @SerialName("num_actions") val numActions: Int? = null,
    @SerialName("num_failed") val numFailed: Int? = null,
    @SerialName("avg_aps") val avgAps: Double? = null,
    @SerialName("aps_target_rate") val apsTargetRate: Double? = null,
    @SerialName("avg_rt") val avgRt: Double? = null,
    @SerialName("sdev_rt") val sdevRt: Double? = null,
    @SerialName("min_rt") val minRt: Double? = null,
    @SerialName("max_rt") val maxRt: Double? = null,
    @SerialName("max_rt_ts") val maxRtTs: String? = null,
    @SerialName("percentiles_rt") val percentilesRt: Map<String, Double>? = null,
    @SerialName("hdr_histogram_rt") val hdrHistogramRt: String? = null,
)

/**
 * Results for a single benchmark iteration.
 */
@Serializable
data class BenchmarkResult(
    @SerialName("context_name") val contextName: String? = null,
    @SerialName("context_id") val contextId: Int? = null,
    @SerialName("bm_name") val bmName: String? = null,
    @SerialName("bm_id") val bmId: Int? = null,
    @SerialName("row_id") val rowId: Int? = null,
    @SerialName("num_users") val numUsers: Int? = null,
    @SerialName("num_tasks") val numTasks: Int? = null,
    @SerialName("num_threads") val numThreads: Int? = null,
    @SerialName("queue_length") val queueLength: Int? = null,
    @SerialName("workflow_name") val workflowName: String? = null,
    @SerialName("test_begin") val testBegin: String? = null,
    @SerialName("test_end") val testEnd: String? = null,
    @SerialName("duration") val duration: Double? = null,
    @SerialName("jvm_memory_used") val jvmMemoryUsed: Long? = null,
    @SerialName("jvm_memory_free") val jvmMemoryFree: Long? = null,
    @SerialName("jvm_memory_total") val jvmMemoryTotal: Long? = null,
    @SerialName("jvm_memory_maximum") val jvmMemoryMaximum: Long? = null,
    @SerialName("process_cpu_utilization") val processCpuUtilization: Double? = null,
    @SerialName("process_cpu_cores") val processCpuCores: Double? = null,
    @SerialName("process_cpu_time_ns") val processCpuTimeNs: Long? = null,
    @SerialName("process_cgc_time_ns") val memoryCpuTimeNs: Long? = null,
    @SerialName("avg_wthread_qsize") val avgWthreadQsize: Double? = null,
    @SerialName("max_wthread_qsize") val maxWthreadQsize: Long? = null,
    @SerialName("avg_wt") val avgWt: Double? = null,
    @SerialName("max_wt") val maxWt: Double? = null,
    @SerialName("num_actions") val numActions: Int? = null,
    @SerialName("num_failed") val numFailed: Int? = null,
    @SerialName("avg_aps") val avgAps: Double? = null,
    @SerialName("aps_target_rate") val apsTargetRate: Double? = null,
    @SerialName("avg_rt") val avgRt: Double? = null,
    @SerialName("sd_rt") val sdevRt: Double? = null,
    @SerialName("min_rt") val minRt: Double? = null,
    @SerialName("max_rt") val maxRt: Double? = null,
    @SerialName("max_rt_ts") val maxRtTs: String? = null,
    @SerialName("percentiles_rt") val percentilesRt: Map<String, Double>? = null,
    @SerialName("hdr_histogram_rt") val hdrHistogramRt: String? = null,
    @SerialName("user_actions") val userActions: Map<String, ActionStatResult>? = null,
)

/**
 * Data class representing aggregated statistics for a benchmark or action.
 */
data class AggregatedStats(
    val numUsers: Int,
    val numActions: Long,
    val numFailed: Long,
    val duration: Double,
    val avgAps: Double,
    val avgRt: Double,
    val sdevRt: Double,
    val minRt: Double,
    val p50: Double,
    val p90: Double,
    val p95: Double,
    val p99: Double,
    val maxRt: Double,
)

/**
 * Configuration for chart rendering.
 */
data class ReportChartConfig(
    val id: String,
    val labels: String,
    val data: String,
    val title: String,
    val unit: String,
    val type: String,
)
