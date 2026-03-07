package io.github.tulipltt.tulip.core

data class Task(
    // The user ID of the user object to which an operation should be applied.
    var userId: Int = -1,
    // Total number of user objects.
    var numUsers: Int = -1,
    // Total number of work threads servicing user objects.
    var numThreads: Int = -1,
    // Numeric id of the action (operation) to be invoked on a user object.
    var actionId: Int = -1,
    // Duration (elapsed time) in microseconds.
    var serviceTimeNanos: Long = 0,
    var waitTimeNanos: Long = 0,
    var rspQueue: MPSC_Queue<Task>? = null,
    var status: Int = -1,
    var beginQueueTimeNanos: Long = 0,
)
