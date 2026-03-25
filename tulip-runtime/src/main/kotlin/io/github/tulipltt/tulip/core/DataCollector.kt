package io.github.tulipltt.tulip.core

import java.io.BufferedWriter
import java.io.FileWriter
import java.time.LocalDateTime
import kotlinx.serialization.json.Json

object DataCollector {
    private var lock: String = "lock"
    private var fileWriteId: Int = 0
    private val actionStats = Array(NUM_ACTIONS + 1) { ActionStats() }

    // val a = arrayOf<Array<ActionStats>>()
    // init {
    //     for (i in 0..3) {
    //         a += actionStats
    //     }
    // }

    fun createSummary(
        durationMillis: Int,
        testCase: TestProfile,
        indexTestCase: Int,
        indexUserProfile: Int,
        queueLength: Int,
        tsBegin: String,
        tsEnd: String,
        testPhase: String,
        runId: Int,
        cpuTime: Long,
        memTime: Long,
        apsTarget: Double,
    ) {
        synchronized(lock) {
            actionStats[NUM_ACTIONS].createSummary(
                NUM_ACTIONS,
                durationMillis,
                testCase,
                indexTestCase,
                queueLength,
                tsBegin,
                tsEnd,
                testPhase,
                runId,
                cpuTime,
                memTime,
                apsTarget,
            )
            actionStats.forEachIndexed { index, data ->
                if (data.numActions > 0) {
                    if (index != NUM_ACTIONS) {
                        data.createSummary(
                            index,
                            durationMillis,
                            testCase,
                            indexTestCase,
                            queueLength,
                            tsBegin,
                            tsEnd,
                            testPhase,
                            -1,
                            0L,
                            0L,
                            0.0,
                        )
                    }
                }
            }
        }
    }

    fun printStats() {
        synchronized(lock) { actionStats[NUM_ACTIONS].printStats(NUM_ACTIONS) }
    }

    fun saveStatsJson(filename: String) {
        synchronized(lock) {
            fun outputFilename(): String =
                if (gOutputDirname == "") filename else "$gOutputDirname/$filename"

            if (filename != "") {
                val r = actionStats[NUM_ACTIONS].r

                val globalStat = actionStats[NUM_ACTIONS].toActionStatResult(-1)
                val userActions = mutableMapOf<String, ActionStatResult>()
                actionStats.forEachIndexed { index, data ->
                    if (data.numActions > 0 && index != NUM_ACTIONS) {
                        userActions[index.toString()] = data.toActionStatResult(index)
                    }
                }

                val workflowName = gWorkflow?.name ?: ""
                val rt = Runtime.getRuntime()

                val benchmarkResult =
                    BenchmarkResult(
                        contextName = gTulipContextName,
                        contextId = gTulipContextId,
                        bmName = r.testName,
                        bmId = r.testId,
                        rowId = r.rowId,
                        numUsers = gMaxNumUsers,
                        numTasks = gMaxNumTasks,
                        numThreads = gMaxNumThreads,
                        queueLength = r.queueLength,
                        workflowName = workflowName,
                        testBegin = r.testBegin,
                        testEnd = r.testEnd,
                        duration = r.durationSeconds,
                        jvmMemoryUsed = rt.totalMemory() - rt.freeMemory(),
                        jvmMemoryFree = rt.freeMemory(),
                        jvmMemoryTotal = rt.totalMemory(),
                        jvmMemoryMaximum = rt.maxMemory(),
                        processCpuUtilization = r.processCpuUtilization,
                        processCpuCores = r.processCpuCores,
                        processCpuTimeNs = r.processCpuTime,
                        memoryCpuTimeNs = r.memoryCpuTime,
                        avgWthreadQsize = wthread_queue_stats.mean,
                        maxWthreadQsize = wthread_queue_stats.maxValue,
                        avgWt = r.awt,
                        maxWt = r.maxWt,

                        // Global stats from globalStat
                        numActions = globalStat.numActions,
                        numFailed = globalStat.numFailed,
                        avgAps = globalStat.avgAps,
                        apsTargetRate = globalStat.apsTargetRate,
                        avgRt = globalStat.avgRt,
                        sdevRt = globalStat.sdevRt,
                        minRt = globalStat.minRt,
                        maxRt = globalStat.maxRt,
                        maxRtTs = globalStat.maxRtTs,
                        percentilesRt = globalStat.percentilesRt,
                        hdrHistogramRt = globalStat.hdrHistogramRt,
                        userActions = userActions,
                    )

                val resultJson = Json.encodeToString(benchmarkResult)

                val fw = FileWriter(outputFilename(), true)
                val bw =
                    BufferedWriter(fw).apply {
                        when (fileWriteId) {
                            0 -> {
                                val header =
                                    BenchmarkHeader(
                                        version = VERSION,
                                        timestamp = LocalDateTime.now().format(formatter),
                                        java = getJavaInfo(),
                                        config = gConfig,
                                    )
                                val headerJson = Json.encodeToString(header)
                                // Strip trailing '}' to append 'results' array
                                val partialHeader = headerJson.substring(0, headerJson.length - 1)
                                write(partialHeader)
                                write(", \"results\": [")
                                newLine()
                                write(" $resultJson")
                            }

                            else -> {
                                write(",$resultJson")
                            }
                        }
                        newLine()
                    }
                fileWriteId += 1
                bw.close()
                fw.close()
            }
        }
    }

    fun closeStatsJson(filename: String) {
        synchronized(lock) {
            fun outputFilename(): String =
                if (gOutputDirname == "") filename else "$gOutputDirname/$filename"

            val fw = FileWriter(outputFilename(), true)
            val bw =
                BufferedWriter(fw).apply {
                    write("]")
                    newLine()
                    write("}")
                    newLine()
                }
            bw.close()
            fw.close()
        }
    }

    fun updateStats(task: Task) {
        synchronized(lock) {
            require(task.actionId < NUM_ACTIONS)
            if (task.actionId < 0) {
                // Unused task drained from the rsp queue.
                return
            }
            actionStats[NUM_ACTIONS].updateStats(task)
            actionStats[task.actionId].updateStats(task)

            //        Counter.builder("Tulip")
            //            .tags("action", task.actionId.toString())
            //            .register(registry)
            //            .increment()
            //
            //        Counter.builder("Tulip")
            //            .tags("action", "aps")
            //            .register(registry)
            //            .increment()
        }
    }

    fun clearStats() {
        synchronized(lock) {
            actionStats.forEach { it.clearStats() }
            wthread_wait_stats.reset()
            wthread_queue_stats.reset()
        }
    }
}
