package org.example.user

import io.github.tulipltt.tulip.api.TulipUser
import io.github.tulipltt.tulip.stats.LlqHistogram
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ThreadLocalRandom
import org.HdrHistogram.Histogram as HdrHistogram

class DemoUser : TulipUser() {
    val llqh = LlqHistogram()
    val hdrh = HdrHistogram(3)
    val rnd: ThreadLocalRandom = ThreadLocalRandom.current()
    var num = 1450312123L

    override fun onStart(): Boolean {
        if (userId == 0) {
            val delay1_ = getUserParamValue("delay1")
            if (delay1_.length > 0) {
                delay1 = delay1_.toLong()
            }
            val delay2_ = getUserParamValue("delay2")
            if (delay2_.length > 0) {
                delay2 = delay2_.toLong()
            }
        }
        return true
    }

    override fun action1(): Boolean {
        Thread.sleep(delay1)
        return true
    }

    override fun action2(): Boolean {
        Thread.sleep(delay2)
        return true
    }

    override fun action3(): Boolean = true

    override fun action7(): Boolean {
        num = rnd.nextLong(1, 1_000_000_000L)
        return true
    }

    override fun action8(): Boolean {
        for (i in 1..1000) {
            hdrh.recordValue(num)
        }
        return true
    }

    override fun action9(): Boolean {
        for (i in 1..1000) {
            llqh.recordValue(num)
        }
        return true
    }

    override fun onStop(): Boolean = true

    override fun logger(): Logger = logger

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DemoUser::class.java)
        private var delay1: Long = 0
        private var delay2: Long = 0
    }
}
