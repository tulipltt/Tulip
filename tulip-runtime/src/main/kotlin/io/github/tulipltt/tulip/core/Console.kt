package io.github.tulipltt.tulip.core

object Console : Thread() {
    init {
        // priority = Thread.MAX_PRIORITY
        isDaemon = true
        name = "console-thread"
        start()
    }

    private val q = Java_Queue<MutableList<String>>(100)

    override fun run() {
        while (true) {
            val list: MutableList<String> = q.take()
            for (s in list) println(s)
        }
    }

    @JvmStatic
    fun put(s: String) {
        put(mutableListOf(s))
    }

    @JvmStatic
    fun put(list: MutableList<String>) {
        q.put(list)
    }
}
