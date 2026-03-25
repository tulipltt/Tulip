package io.github.tulipltt.tulip.core

import java.nio.ByteBuffer
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.HdrHistogram.Histogram

val doubleFormatter: NumberFormat =
    NumberFormat.getInstance(Locale.US).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }

val longFormatter: NumberFormat = NumberFormat.getInstance(Locale.US)

class ActionStats {
    // <numberOfSignificantValueDigits>
    // private val NUM_DIGITS=1  // Tested - inaccurate results, don't use
    // private val NUM_DIGITS=2  // Tested - good results, small results file
    // (default, optimal)
    // private val NUM_DIGITS=3  // Tested - great results, large results file,
    // histogram_rt
    private val hdr_histogram: Histogram = Histogram(HDR_NUM_SIGNIFICANT_VALUE_DIGITS)
    private var histogramMinRt: Long = Long.MAX_VALUE
    private var histogramMaxRt: Long = Long.MIN_VALUE
    private var histogramMaxRtTs = ""

    var numActions: Int = 0
    private var numSuccess: Int = 0

    val r = ActionSummary()

    fun formatTime(timeNanos: Double): String {
        if (timeNanos < 1000.0) {
            return String.format(Locale.US, "%.1f ns", timeNanos)
        } else if (timeNanos < 1000000.0) {
            return String.format(Locale.US, "%.1f us", timeNanos / 1000.0)
        } else if (timeNanos < 1000000000.0) {
            return String.format(Locale.US, "%.1f ms", timeNanos / 1000000.0)
        } else {
            return String.format(Locale.US, "%.1f s", timeNanos / 1000000000.0)
        }
    }

    fun createSummary(
        actionId: Int,
        durationMillis: Int,
        testCase: TestProfile,
        indexTestCase: Int,
        queueLength: Int,
        tsBegin: String,
        tsEnd: String,
        testPhase: String,
        runId: Int,
        cpuTime: Long,
        memTime: Long,
        aps_target: Double,
    ) {
        r.actionId = actionId

        r.rowId = runId

        r.testName = testCase.name
        r.testBegin = tsBegin
        r.testEnd = tsEnd
        r.testPhase = testPhase

        r.testId = indexTestCase
        r.queueLength = queueLength

        r.numActions = numActions
        r.numSuccess = numSuccess

        r.durationSeconds = durationMillis.toDouble() / 1000.0

        // actions per second (aps)
        r.aps = numActions / r.durationSeconds
        r.aps_target = aps_target

        // average response time (art) in nanoseconds
        r.art = hdr_histogram.mean

        // standard deviation
        r.sdev = hdr_histogram.stdDeviation

        // min rt
        r.minRt = histogramMinRt * 1.0

        // max rt
        r.maxRt = histogramMaxRt * 1.0

        // max rt timestamp
        r.maxRtTs = histogramMaxRtTs

        // percentiles
        r.pk = testCase.percentiles
        r.pv =
            mutableListOf<Double>().apply {
                r.pk.forEach {
                    var px: Double = hdr_histogram.getValueAtPercentile(it) * 1.0
                    if (px > r.maxRt) {
                        px = r.maxRt
                    }
                    this.add(px)
                }
            }

        r.hdr_histogram = hdr_histogram

        r.awt = wthread_wait_stats.mean
        r.maxWt = wthread_wait_stats.maxValueAsDouble

        // Summarize CPU usage for global stats only.
        // if (actionId == NUM_ACTIONS) {
        //    // average process CPU load.
        //    // r.avgCpuProcess = 0.0
        //
        //    // average system CPU load.
        //    // r.avgCpuSystem = 0.0
        // }
        if (actionId == NUM_ACTIONS) {
            r.processCpuTime = cpuTime
            r.memoryCpuTime = memTime
        }
    }

    fun printStats(actionId: Int) {
        val output = mutableListOf("")

        if (r.processCpuTime == 0L) {
            // Init, or Shutdown -> TPS inaccurate, so set to 0.0
            // r.aps = 0.0
            // This is not a complete solution, breaks the report
            // Disabled for now.
        }

        if (actionId != NUM_ACTIONS) {
            output.add("  action_id = ${r.actionId}")
        }

        if (r.processCpuTime == 0L) {
            // Init, or Shutdown
            output.add("  duration    = ${r.durationSeconds} seconds")
        } else {
            // Benchmark
            output.add("  duration    = ${r.durationSeconds} seconds")
        }
        output.add("  num_actions = ${longFormatter.format(numActions.toLong())}")
        output.add(
            "  num_failed  = ${longFormatter.format((r.numActions - r.numSuccess).toLong())}"
        )
        output.add("")
        output.add("  avg_aps = ${doubleFormatter.format(r.aps)}")
        output.add("  avg_rt  = ${formatTime(r.art)}")
        output.add("  std_dev = ${formatTime(r.sdev)}")
        output.add("  min_rt  = ${formatTime(r.minRt)}")
        output.add(
            "  max_rt  = ${
                formatTime(r.maxRt)
            } at $histogramMaxRtTs"
        )

        if (actionId == NUM_ACTIONS) {
            val rt = Runtime.getRuntime()
            val fm = rt.freeMemory()
            val tm = rt.totalMemory()
            val mm = rt.maxMemory()

            // output.add("")
            // output.add("  average cpu load (process) =
            // ${"%.3f".format(Locale.US, r.avgCpuProcess)}")
            // output.add("  average cpu load (system ) =
            // ${"%.3f".format(Locale.US, r.avgCpuSystem)}")

            // mg_cpu_tulip?.set(r.avgCpuProcess.toInt())
            // mg_cpu_system?.set(r.avgCpuSystem.toInt())

            output.add("")
            val gb1 = 1073741824.0
            output.add("  memory used (jvm)    = ${"%.3f".format(Locale.US, (tm - fm) / gb1)} GB")
            output.add("  free memory (jvm)    = ${"%.3f".format(Locale.US, fm / gb1)} GB")
            output.add("  total memory (jvm)   = ${"%.3f".format(Locale.US, tm / gb1)} GB")
            output.add("  maximum memory (jvm) = ${"%.3f".format(Locale.US, mm / gb1)} GB")
            output.add("")
            val cpu_time_secs: Double = r.processCpuTime / 1000000000.0
            val cgc_time_secs: Double = r.memoryCpuTime / 1000000000.0
            output.add(
                "  cpu time (process)   = ${"%.3f".format(Locale.US, cpu_time_secs)} seconds"
            )
            if (java.lang.Runtime.version().feature() > 25) {
                output.add(
                    "  cgc time (process)   = ${"%.3f".format(Locale.US, cgc_time_secs)} seconds"
                )
            }
            r.processCpuCores = cpu_time_secs / r.durationSeconds
            output.add(
                "  num cores used       = ${"%.3f".format(Locale.US, r.processCpuCores)} cores"
            )
            r.processCpuUtilization = 100.0 * r.processCpuCores
            // if (r.processCpuUtilization > 100.0) r.processCpuUtilization =
            // 100.0
            // support > 100.0 utilization due to hyper-threading
            output.add(
                "  avg cpu utilization  = ${"%.1f".format(Locale.US, r.processCpuUtilization)}"
            )
            // output.add("  avg process cpu load = ${"%.1f".format(Locale.US,
            // getProcessCpuLoad())}")
            // output.add("  avg  system cpu load = ${"%.1f".format(Locale.US, getCpuLoad())}")

            //            output.add("")
            //            val awqs: Double = wthread_queue_stats.mean
            //            val mwqs: Long = wthread_queue_stats.maxValue
            //            output.add("  avg wkr thrd qsize =
            // ${"%.3f".format(Locale.US, awqs)}")
            //            output.add("  max wkr thrd qsize =
            // ${"%,d".format(Locale.US, mwqs)}")
            //            output.add("  average wait time  =
            // ${"%.3f".format(Locale.US, r.awt)} ms")
            //            output.add("  maximum wait time  =
            // ${"%.3f".format(Locale.US, r.maxWt)} ms")

            //            mg_rt_avg?.set(r.art.toInt())
            //            mg_rt_max?.set(r.maxRt.toInt())
            //            mg_rt_min?.set(r.minRt.toInt())
            //
            //            mg_num_actions?.set(r.numActions)
            //            mg_num_failed?.set(r.numActions - r.numSuccess)
            //
            //            mg_benchmark_aps?.set(r.aps.toInt())
            //            mg_benchmark_dur?.set(r.durationSeconds.toInt())
            //            var phaseId = 0
            //            if (r.testPhase == "PreWarmup") phaseId = 0
            //            if (r.testPhase == "Warmup") phaseId = 1
            //            if (r.testPhase == "Benchmark") phaseId = 2

            //            mg_benchmark_phs?.set(phaseId)
            //            mg_benchmark_run?.set(r.rowId)
        }

        Console.put(output)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun toActionStatResult(actionId: Int): ActionStatResult {
        val name: String? =
            if (actionId >= 0) {
                if (actionNames.containsKey(actionId)) {
                    actionNames[actionId]!!
                } else {
                    "action$actionId"
                }
            } else {
                null
            }

        val b = ByteBuffer.allocate(hdr_histogram.neededByteBufferCapacity)
        val numBytes = hdr_histogram.encodeIntoCompressedByteBuffer(b)
        val b64s = Base64.encode(b.array(), 0, numBytes)

        val pkMap = mutableMapOf<String, Double>()
        r.pk.forEachIndexed { index, _ -> pkMap[r.pk[index].toString()] = r.pv[index] }

        return ActionStatResult(
            name = name,
            numActions = numActions,
            numFailed = numActions - numSuccess,
            avgAps = r.aps,
            apsTargetRate = r.aps_target,
            avgRt = r.art,
            sdevRt = r.sdev,
            minRt = r.minRt,
            maxRt = r.maxRt,
            maxRtTs = r.maxRtTs,
            percentilesRt = pkMap,
            hdrHistogramRt = b64s,
        )
    }

    fun updateStats(task: Task) {
        // Workaround, as we cannot store 0 in the HdrHistogram
        val durationNanos = task.serviceTimeNanos
        if (durationNanos > 0L) {
            hdr_histogram.recordValue(durationNanos)
            // llq_histogram.recordValue(durationNanos)
        }
        if (durationNanos < histogramMinRt) {
            histogramMinRt = durationNanos
        }
        if (durationNanos > histogramMaxRt) {
            histogramMaxRt = durationNanos
            histogramMaxRtTs = LocalDateTime.now().format(formatter)
        }
        numActions += 1
        if (task.status == 1) {
            numSuccess += 1
        }
        wthread_wait_stats.recordValue(task.waitTimeNanos)
    }

    fun clearStats() {
        hdr_histogram.reset()
        histogramMinRt = Long.MAX_VALUE
        histogramMaxRt = Long.MIN_VALUE
        histogramMaxRtTs = ""
        numActions = 0
        numSuccess = 0
    }
}
