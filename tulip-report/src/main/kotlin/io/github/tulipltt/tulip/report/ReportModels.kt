package io.github.tulipltt.tulip.report

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JavaInfo(
    @SerialName("jvm.system.properties") val systemProperties: Map<String, String> = emptyMap(),
    @SerialName("jvm.runtime.options") val runtimeOptions: List<String> = emptyList(),
)

@Serializable
data class ActionStatResult(
    val name: String? = null,
    @SerialName("num_actions") val numActions: Int = 0,
    @SerialName("num_failed") val numFailed: Int = 0,
    @SerialName("avg_aps") val avgAps: Double = 0.0,
    @SerialName("aps_target_rate") val apsTargetRate: Double = 0.0,
    @SerialName("avg_rt") val avgRt: Double = 0.0,
    @SerialName("sdev_rt") val sdevRt: Double = 0.0,
    @SerialName("min_rt") val minRt: Double = 0.0,
    @SerialName("max_rt") val maxRt: Double = 0.0,
    @SerialName("max_rt_ts") val maxRtTs: String = "",
    @SerialName("percentiles_rt") val percentilesRt: Map<String, Double> = emptyMap(),
    @SerialName("hdr_histogram_rt") val hdrHistogramRt: String = "",
)

@Serializable
data class BenchmarkResult(
    @SerialName("context_name") val contextName: String = "",
    @SerialName("context_id") val contextId: Int = 0,
    @SerialName("bm_name") val bmName: String = "",
    @SerialName("bm_id") val bmId: Int = 0,
    @SerialName("row_id") val rowId: Int = 0,
    @SerialName("num_users") val numUsers: Int = 0,
    @SerialName("num_tasks") val numTasks: Int = 0,
    @SerialName("num_threads") val numThreads: Int = 0,
    @SerialName("queue_length") val queueLength: Int = 0,
    @SerialName("workflow_name") val workflowName: String = "",
    @SerialName("test_begin") val testBegin: String = "",
    @SerialName("test_end") val testEnd: String = "",
    @SerialName("duration") val duration: Double = 0.0,
    @SerialName("jvm_memory_used") val jvmMemoryUsed: Long = 0L,
    @SerialName("jvm_memory_free") val jvmMemoryFree: Long = 0L,
    @SerialName("jvm_memory_total") val jvmMemoryTotal: Long = 0L,
    @SerialName("jvm_memory_maximum") val jvmMemoryMaximum: Long = 0L,
    @SerialName("process_cpu_utilization") val processCpuUtilization: Double = 0.0,
    @SerialName("process_cpu_cores") val processCpuCores: Double = 0.0,
    @SerialName("process_cpu_time_ns") val processCpuTimeNs: Long = 0L,
    @SerialName("process_cgc_time_ns") val memoryCpuTimeNs: Long = 0L,
    @SerialName("avg_wthread_qsize") val avgWthreadQsize: Double = 0.0,
    @SerialName("max_wthread_qsize") val maxWthreadQsize: Long = 0L,
    @SerialName("avg_wt") val avgWt: Double = 0.0,
    @SerialName("max_wt") val maxWt: Double = 0.0,

    // Global action stats (flattened from ActionStatResult)
    @SerialName("num_actions") val numActions: Int = 0,
    @SerialName("num_failed") val numFailed: Int = 0,
    @SerialName("avg_aps") val avgAps: Double = 0.0,
    @SerialName("aps_target_rate") val apsTargetRate: Double = 0.0,
    @SerialName("avg_rt") val avgRt: Double = 0.0,
    @SerialName("sd_rt") val sdevRt: Double = 0.0,
    @SerialName("min_rt") val minRt: Double = 0.0,
    @SerialName("max_rt") val maxRt: Double = 0.0,
    @SerialName("max_rt_ts") val maxRtTs: String = "",
    @SerialName("percentiles_rt") val percentilesRt: Map<String, Double> = emptyMap(),
    @SerialName("hdr_histogram_rt") val hdrHistogramRt: String = "",
    @SerialName("user_actions") val userActions: Map<String, ActionStatResult> = emptyMap(),
)
