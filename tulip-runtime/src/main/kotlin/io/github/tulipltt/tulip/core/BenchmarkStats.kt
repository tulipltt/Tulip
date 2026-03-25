package io.github.tulipltt.tulip.core

import java.lang.management.ManagementFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class JavaInfo(
    @SerialName("jvm.system.properties") val systemProperties: Map<String, String>,
    @SerialName("jvm.runtime.options") val runtimeOptions: List<String>,
)

fun getJavaInfo(): JavaInfo {
    val systemProperties =
        mapOf(
            "java.vendor" to System.getProperty("java.vendor"),
            "java.version" to System.getProperty("java.version"),
            "java.runtime.version" to System.getProperty("java.runtime.version"),
            "os.name" to System.getProperty("os.name"),
            "os.arch" to System.getProperty("os.arch"),
        )
    val jvmArgs =
        ManagementFactory.getRuntimeMXBean().inputArguments.distinct().map { it.replace("\\", "/") }
    return JavaInfo(systemProperties, jvmArgs)
}

fun getJavaInfoJson(): String {
    return Json.encodeToString(getJavaInfo())
}

@Serializable
data class ActionStatResult(
    val name: String? = null,
    @SerialName("num_actions") val numActions: Int,
    @SerialName("num_failed") val numFailed: Int,
    @SerialName("avg_aps") val avgAps: Double,
    @SerialName("aps_target_rate") val apsTargetRate: Double,
    @SerialName("avg_rt") val avgRt: Double,
    @SerialName("sdev_rt") val sdevRt: Double,
    @SerialName("min_rt") val minRt: Double,
    @SerialName("max_rt") val maxRt: Double,
    @SerialName("max_rt_ts") val maxRtTs: String,
    @SerialName("percentiles_rt") val percentilesRt: Map<String, Double>,
    @SerialName("hdr_histogram_rt") val hdrHistogramRt: String,
)

@Serializable
data class BenchmarkResult(
    @SerialName("context_name") val contextName: String,
    @SerialName("context_id") val contextId: Int,
    @SerialName("bm_name") val bmName: String,
    @SerialName("bm_id") val bmId: Int,
    @SerialName("row_id") val rowId: Int,
    @SerialName("num_users") val numUsers: Int,
    @SerialName("num_tasks") val numTasks: Int,
    @SerialName("num_threads") val numThreads: Int,
    @SerialName("queue_length") val queueLength: Int,
    @SerialName("workflow_name") val workflowName: String,
    @SerialName("test_begin") val testBegin: String,
    @SerialName("test_end") val testEnd: String,
    @SerialName("duration") val duration: Double,
    @SerialName("jvm_memory_used") val jvmMemoryUsed: Long,
    @SerialName("jvm_memory_free") val jvmMemoryFree: Long,
    @SerialName("jvm_memory_total") val jvmMemoryTotal: Long,
    @SerialName("jvm_memory_maximum") val jvmMemoryMaximum: Long,
    @SerialName("process_cpu_utilization") val processCpuUtilization: Double,
    @SerialName("process_cpu_cores") val processCpuCores: Double,
    @SerialName("process_cpu_time_ns") val processCpuTimeNs: Long,
    @SerialName("process_cgc_time_ns") val memoryCpuTimeNs: Long,
    @SerialName("avg_wthread_qsize") val avgWthreadQsize: Double,
    @SerialName("max_wthread_qsize") val maxWthreadQsize: Long,
    @SerialName("avg_wt") val avgWt: Double,
    @SerialName("max_wt") val maxWt: Double,

    // Global action stats (flattened from ActionStatResult)
    @SerialName("num_actions") val numActions: Int,
    @SerialName("num_failed") val numFailed: Int,
    @SerialName("avg_aps") val avgAps: Double,
    @SerialName("aps_target_rate") val apsTargetRate: Double,
    @SerialName("avg_rt") val avgRt: Double,
    @SerialName("sdev_rt") val sdevRt: Double,
    @SerialName("min_rt") val minRt: Double,
    @SerialName("max_rt") val maxRt: Double,
    @SerialName("max_rt_ts") val maxRtTs: String,
    @SerialName("percentiles_rt") val percentilesRt: Map<String, Double>,
    @SerialName("hdr_histogram_rt") val hdrHistogramRt: String,
    @SerialName("user_actions") val userActions: Map<String, ActionStatResult>,
)

@Serializable
data class BenchmarkHeader(
    val version: String,
    val timestamp: String,
    val java: JavaInfo,
    val config: TulipConfig,
)
