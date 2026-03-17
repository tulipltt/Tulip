package io.github.tulipltt.tulip.core

import io.github.tulipltt.tulip.api.TulipApi
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
                val rt = Runtime.getRuntime()
                val fm = rt.freeMemory()
                val tm = rt.totalMemory()
                val mm = rt.maxMemory()

                val r = actionStats[NUM_ACTIONS].r

                var json = "{"

                val workflowName: String
                if (gWorkflow == null) {
                    workflowName = ""
                } else {
                    workflowName = gWorkflow!!.name
                }

                json += "\"context_name\": \"$gTulipContextName\", "
                json += "\"context_id\": $gTulipContextId, "
                json += "\"bm_name\": \"${r.testName}\", "
                json += "\"bm_id\": ${r.testId}, "
                json += "\"row_id\": ${r.rowId}, "

                json += "\"num_users\": $gMaxNumUsers, "
                json += "\"num_tasks\": $gMaxNumTasks, "
                json += "\"num_threads\": $gMaxNumThreads, "
                json += "\"queue_length\": ${r.queueLength}, "

                json += "\"workflow_name\": \"$workflowName\", "

                json += "\"test_begin\": \"${r.testBegin}\", "
                json += "\"test_end\": \"${r.testEnd}\", "

                json += "\"duration\": ${r.durationSeconds}, "

                // json += "\"avg_cpu_process\": ${r.avgCpuProcess},
                // \"avg_cpu_system\": ${r.avgCpuSystem}, "

                json += "\"jvm_memory_used\": ${tm - fm}, "
                json += "\"jvm_memory_free\": $fm, "
                json += "\"jvm_memory_total\": $tm, "
                json += "\"jvm_memory_maximum\": $mm, "

                json += "\"process_cpu_utilization\": ${r.processCpuUtilization}, "
                json += "\"process_cpu_cores\": ${r.processCpuCores}, "
                json += "\"process_cpu_time_ns\": ${r.processCpuTime},"
                json += "\"process_cgc_time_ns\": ${r.memoryCpuTime}"

                val awqs: Double = wthread_queue_stats.mean
                val mwqs: Long = wthread_queue_stats.maxValue
                json += ", \"avg_wthread_qsize\": $awqs"
                json += ", \"max_wthread_qsize\": $mwqs"

                json += ", \"avg_wt\": ${r.awt}, \"max_wt\": ${r.maxWt}"

                json += actionStats[NUM_ACTIONS].toJson(-1)

                json += ", \"user_actions\": {"

                var t = ""
                actionStats.forEachIndexed { index, data ->
                    if (data.numActions > 0) {
                        if (index != NUM_ACTIONS) {
                            if (t != "") {
                                t += ","
                            }
                            t += "\"${index}\": {" + data.toJson(index) + "}"
                        }
                    }
                }
                json += t
                json += "}"

                json += "}"

                val fw = FileWriter(outputFilename(), true)
                val bw =
                    BufferedWriter(fw).apply {
                        when (fileWriteId) {
                            0 -> {
                                // val gson =
                                // GsonBuilder().setPrettyPrinting().create()
                                // val jsonString = gson.toJson(g_config)
                                val jsonString = Json.encodeToString(gConfig)
                                // val jsonString = "${g_config}"
                                write("{  ")
                                newLine()
                                write("  \"version\": \"${VERSION}\"")
                                newLine()
                                write(
                                    ", \"timestamp\": \"${LocalDateTime.now().format(formatter)}\""
                                )
                                newLine()
                                write(", \"java\": ${TulipApi.getJavaInformation()}")
                                newLine()
                                write(", \"config\": $jsonString")
                                newLine()
                                write(", \"results\": [")
                                newLine()
                                write(" $json")
                            }

                            else -> {
                                write(",$json")
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
