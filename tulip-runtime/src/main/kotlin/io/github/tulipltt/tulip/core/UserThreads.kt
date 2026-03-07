package io.github.tulipltt.tulip.core

import io.github.tulipltt.tulip.api.TulipUser
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.time.Clock
import kotlin.time.Instant
import org.HdrHistogram.IntCountsHistogram

var useVirtualThreads = false
val executor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
var userPlatformThreads: Array<UserPlatformThread?>? = null // arrayOfNulls<UserThread>(NUM_THREADS)
var userObjects: Array<TulipUser?>? = null // arrayOfNulls<User>(NUM_USERS)

const val USER_THREAD_QSIZE = 11

class UserPlatformThread(private val threadId: Int) : Thread() {
    init {
        name = "w$threadId"
    }

    //
    // Task Queue - input queue with tasks for this thread to complete.
    //
    val tq = SPSC_Queue<Task>(USER_THREAD_QSIZE)
    private var running = true

    override fun run() {
        while (running) {
            //
            // Wait for a new task to be assigned to this thread.
            //
            val task: Task = tq.take()

            // / ....
            if (task.status == 999) {
                // Console.put("Thread ${name} is stopping.")
                running = false
            } else {
                //
                // Locate the user object to which the task should be applied.
                // Dynamically create a new user object, if required.
                //
                var u = userObjects!![task.userId]
                if (u == null) {
                    u = newUser!!.getUser(gConfig.actions.userClass, task.userId, threadId)
                    userObjects!![task.userId] = u
                }

                //
                // Apply the task to the user object. The return value is either
                // True or False, indicating that the task succeeded or failed.
                // Also calculate the elapsed time in microseconds.
                //
                u.processTask(task)

                task.rspQueue!!.put(task)
            }
        }
        userPlatformThreads!![threadId] = null
    }
}

val wthread_queue_stats = IntCountsHistogram(HDR_NUM_SIGNIFICANT_VALUE_DIGITS)

fun assignTaskToUserPT(task: Task) {
    val threadId = task.userId / (task.numUsers / task.numThreads)
    var w = userPlatformThreads!![threadId]
    if (w == null) {
        w =
            UserPlatformThread(threadId).apply {
                isDaemon = true
                start()
            }
        userPlatformThreads!![threadId] = w
    }
    task.beginQueueTimeNanos = System.nanoTime()
    if (!w.tq.offer(task)) {
        // We know the queue is full, so queue size = queue capacity
        w.tq.put(task)
        // No locking required, just reading of property capacity.
        wthread_queue_stats.recordValue(w.tq.capacity.toLong())
    } else {
        // Grab a reentrant lock and read the size property.
        wthread_queue_stats.recordValue(w.tq.size.toLong())
    }
}

fun assignTaskToUserVT(task: Task) {
    var u = userObjects!![task.userId]
    if (u == null) {
        u = newUser!!.getUser(gConfig.actions.userClass, task.userId, 0)
        userObjects!![task.userId] = u
        val user = u
        user.future = executor.submit(Runnable { user.run() })
    }
    val tq = u.tq
    task.beginQueueTimeNanos = System.nanoTime()
    if (!tq.offer(task)) {
        // We know the queue is full, so queue size = queue capacity
        tq.put(task)
        // No locking required, just reading of property capacity.
        wthread_queue_stats.recordValue(tq.capacity.toLong())
    } else {
        // Grab a reentrant lock and read the size property.
        wthread_queue_stats.recordValue(tq.size.toLong())
    }
}

fun assignTaskToUser(task: Task) {
    if (useVirtualThreads) {
        assignTaskToUserVT(task)
    } else {
        assignTaskToUserPT(task)
    }
}

fun runtimeDonePT() {
    // Terminate all platform threads.
    userPlatformThreads!!.forEach { thread ->
        thread!!.tq.clear()
        thread.tq.put(Task(status = 999))
    }

    // Wait for all platform threads to exit.
    while (userPlatformThreads!!.map { if (it == null) 0 else 1 }.sum() > 0) {
        Thread.sleep(500)
    }
}

fun displayTimestamp() {
    val now: Instant = Clock.System.now()
    println("Current Instant: $now")
}

fun runtimeDoneVT() {
    // Terminate all virtual threads.
    userObjects!!.forEach { user ->
        user!!.tq.clear()
        user.tq.put(Task(status = 999))
    }
    // Wait for all virtual threads to exit.
    userObjects!!.forEach { user -> user!!.future!!.get() }
}

fun runtimeDone() {
    if (useVirtualThreads) {
        runtimeDoneVT()
    } else {
        runtimeDonePT()
    }
}

// https://ericnormand.me/guide/clojure-virtual-threads
// - Don't access synchronized blocks or methods
