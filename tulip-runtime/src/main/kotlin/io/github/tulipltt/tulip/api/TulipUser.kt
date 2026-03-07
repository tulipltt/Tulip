package io.github.tulipltt.tulip.api

import io.github.tulipltt.tulip.core.InformativeBlockingQueue as SPSC_Queue
import io.github.tulipltt.tulip.core.Task
import io.github.tulipltt.tulip.core.USER_THREAD_QSIZE
import io.github.tulipltt.tulip.core.actionNames
import io.github.tulipltt.tulip.core.elapsedTimeNanos
import io.github.tulipltt.tulip.core.gConfig
import io.github.tulipltt.tulip.core.gWorkflow
import io.github.tulipltt.tulip.core.userRuntimeContext
import java.util.concurrent.Future
import kotlinx.serialization.json.JsonPrimitive
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class TulipUser {
    var userId: Int = -1
    var threadId: Int = -1

    fun initRuntime(userId: Int, threadId: Int) {
        this.userId = userId
        this.threadId = threadId
    }

    val tq = SPSC_Queue<Task>(USER_THREAD_QSIZE)
    var future: Future<*>? = null

    private var aid: Int = 0

    private val map =
        arrayOf(
            ::onStart,
            ::action1,
            ::action2,
            ::action3,
            ::action4,
            ::action5,
            ::action6,
            ::action7,
            ::action8,
            ::action9,
            ::action10,
            ::action11,
            ::action12,
            ::action13,
            ::action14,
            ::action15,
            ::action16,
            ::action17,
            ::action18,
            ::action19,
            ::action20,
            ::action21,
            ::action22,
            ::action23,
            ::action24,
            ::action25,
            ::action26,
            ::action27,
            ::action28,
            ::action29,
            ::action30,
            ::action31,
            ::action32,
            ::action33,
            ::action34,
            ::action35,
            ::action36,
            ::action37,
            ::action38,
            ::action39,
            ::action40,
            ::action41,
            ::action42,
            ::action43,
            ::action44,
            ::action45,
            ::action46,
            ::action47,
            ::action48,
            ::action49,
            ::action50,
            ::action51,
            ::action52,
            ::action53,
            ::action54,
            ::action55,
            ::action56,
            ::action57,
            ::action58,
            ::action59,
            ::action60,
            ::action61,
            ::action62,
            ::action63,
            ::action64,
            ::action65,
            ::action66,
            ::action67,
            ::action68,
            ::action69,
            ::action70,
            ::action71,
            ::action72,
            ::action73,
            ::action74,
            ::action75,
            ::action76,
            ::action77,
            ::action78,
            ::action79,
            ::action80,
            ::action81,
            ::action82,
            ::action83,
            ::action84,
            ::action85,
            ::action86,
            ::action87,
            ::action88,
            ::action89,
            ::action90,
            ::action91,
            ::action92,
            ::action93,
            ::action94,
            ::action95,
            ::action96,
            ::action97,
            ::action98,
            ::action99,
            ::onStop,
        )

    abstract fun onStart(): Boolean

    open fun action1(): Boolean = false

    open fun action2(): Boolean = false

    open fun action3(): Boolean = false

    open fun action4(): Boolean = false

    open fun action5(): Boolean = false

    open fun action6(): Boolean = false

    open fun action7(): Boolean = false

    open fun action8(): Boolean = false

    open fun action9(): Boolean = false

    open fun action10(): Boolean = false

    open fun action11(): Boolean = false

    open fun action12(): Boolean = false

    open fun action13(): Boolean = false

    open fun action14(): Boolean = false

    open fun action15(): Boolean = false

    open fun action16(): Boolean = false

    open fun action17(): Boolean = false

    open fun action18(): Boolean = false

    open fun action19(): Boolean = false

    open fun action20(): Boolean = false

    open fun action21(): Boolean = false

    open fun action22(): Boolean = false

    open fun action23(): Boolean = false

    open fun action24(): Boolean = false

    open fun action25(): Boolean = false

    open fun action26(): Boolean = false

    open fun action27(): Boolean = false

    open fun action28(): Boolean = false

    open fun action29(): Boolean = false

    open fun action30(): Boolean = false

    open fun action31(): Boolean = false

    open fun action32(): Boolean = false

    open fun action33(): Boolean = false

    open fun action34(): Boolean = false

    open fun action35(): Boolean = false

    open fun action36(): Boolean = false

    open fun action37(): Boolean = false

    open fun action38(): Boolean = false

    open fun action39(): Boolean = false

    open fun action40(): Boolean = false

    open fun action41(): Boolean = false

    open fun action42(): Boolean = false

    open fun action43(): Boolean = false

    open fun action44(): Boolean = false

    open fun action45(): Boolean = false

    open fun action46(): Boolean = false

    open fun action47(): Boolean = false

    open fun action48(): Boolean = false

    open fun action49(): Boolean = false

    open fun action50(): Boolean = false

    open fun action51(): Boolean = false

    open fun action52(): Boolean = false

    open fun action53(): Boolean = false

    open fun action54(): Boolean = false

    open fun action55(): Boolean = false

    open fun action56(): Boolean = false

    open fun action57(): Boolean = false

    open fun action58(): Boolean = false

    open fun action59(): Boolean = false

    open fun action60(): Boolean = false

    open fun action61(): Boolean = false

    open fun action62(): Boolean = false

    open fun action63(): Boolean = false

    open fun action64(): Boolean = false

    open fun action65(): Boolean = false

    open fun action66(): Boolean = false

    open fun action67(): Boolean = false

    open fun action68(): Boolean = false

    open fun action69(): Boolean = false

    open fun action70(): Boolean = false

    open fun action71(): Boolean = false

    open fun action72(): Boolean = false

    open fun action73(): Boolean = false

    open fun action74(): Boolean = false

    open fun action75(): Boolean = false

    open fun action76(): Boolean = false

    open fun action77(): Boolean = false

    open fun action78(): Boolean = false

    open fun action79(): Boolean = false

    open fun action80(): Boolean = false

    open fun action81(): Boolean = false

    open fun action82(): Boolean = false

    open fun action83(): Boolean = false

    open fun action84(): Boolean = false

    open fun action85(): Boolean = false

    open fun action86(): Boolean = false

    open fun action87(): Boolean = false

    open fun action88(): Boolean = false

    open fun action89(): Boolean = false

    open fun action90(): Boolean = false

    open fun action91(): Boolean = false

    open fun action92(): Boolean = false

    open fun action93(): Boolean = false

    open fun action94(): Boolean = false

    open fun action95(): Boolean = false

    open fun action96(): Boolean = false

    open fun action97(): Boolean = false

    open fun action98(): Boolean = false

    open fun action99(): Boolean = false

    abstract fun onStop(): Boolean

    open fun processAction(actionId: Int): Boolean =
        try {
            map[actionId]()
        } catch (e: Exception) {
            val msg = "userId: $userId, actionId: $actionId"
            logger().error(msg, e)
            false
        }

    open fun nextAction(workflowId: Int): Int {
        aid = gWorkflow!!.next(aid)
        if (aid == 0) {
            aid = gWorkflow!!.next(aid)
        }
        return aid
    }

    open fun getUserParamValue(paramName: String): String {
        val o: JsonPrimitive =
            userRuntimeContext.userParams[paramName]
                ?: gConfig.actions.userParams[paramName]
                ?: return ""
        var s = o.toString() // s = '"value"', the double quotes must be removed
        if (o.isString) {
            // s = '"value"', the double quotes must be removed
            s = s.substring(1, s.length - 1)
        }
        return s
    }

    open fun getActionName(actionId: Int): String =
        if (actionNames.containsKey(actionId)) {
            actionNames[actionId]!!
        } else {
            when (actionId) {
                0 -> "onStart"
                TulipApi.NUM_ACTIONS - 1 -> "opStop"
                else -> "action$actionId"
            }
        }

    open fun processTask(task: Task) {
        task.waitTimeNanos = System.nanoTime() - task.beginQueueTimeNanos
        if (task.actionId < 0) {
            // Use Markov Chain to determine next action to perform.
            task.actionId = nextAction(task.actionId)
        }
        task.serviceTimeNanos = elapsedTimeNanos {
            if (processAction(task.actionId)) {
                task.status = 1
            } else {
                task.status = 0
            }
        }
    }

    open fun run() {
        var running = true
        while (running) {
            val task: Task = tq.take()
            if (task.status == 999) {
                running = false
            } else {
                processTask(task)
                task.rspQueue!!.put(task)
            }
        }
    }

    open fun logger(): Logger = logger

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TulipUser::class.java)
    }
}
