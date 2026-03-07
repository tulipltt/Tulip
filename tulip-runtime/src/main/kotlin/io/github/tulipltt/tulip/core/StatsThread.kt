package io.github.tulipltt.tulip.core

class StatsThread(val itq: MPSC_Queue<Task>, val otq: MPSC_Queue<Task>) : Thread() {
    init {
        name = "stats-thread"
    }

    var running = true

    override fun run() {
        // Console.put("Thread ${name} is starting.")
        while (running) {
            //
            // Wait for a new task to be assigned to this thread.
            //
            val task: Task = itq.take()

            if (task.status == 999) {
                // Console.put("Thread ${name} is stopping.")
                running = false
            } else {
                DataCollector.updateStats(task)
                otq.put(task)
            }
        }
    }
}
