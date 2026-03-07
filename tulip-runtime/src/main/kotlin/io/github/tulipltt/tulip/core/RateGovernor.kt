package io.github.tulipltt.tulip.core

import java.util.concurrent.TimeUnit

class RateGovernor(
    private val averageRate: Double,
    private val timeMillisStart: Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()),
) {
    private var count: Long = 0

    fun pace() {
        count += 1
        val deltaMs: Long =
            (timeMillisStart + count * (1000 / averageRate) -
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime()))
                .toLong()
        if (deltaMs > 0) Thread.sleep(deltaMs)
    }
}
