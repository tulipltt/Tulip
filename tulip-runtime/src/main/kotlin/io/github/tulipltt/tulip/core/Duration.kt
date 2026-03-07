package io.github.tulipltt.tulip.core

import java.util.concurrent.TimeUnit

data class Duration(
    // Start-up only executed once per TestCase.
    val startupDurationUnits: Long = 0,
    // Warm-up phase executed once per TestCase.
    val warmupDurationUnits: Long = 0,
    // Main duration in minutes.
    // The results from this period are reported.
    //
    // The main phase executed 'repeatCount' every iteration of TestCase.
    val mainDurationUnits: Long = 0,
    val mainDurationRepeatCount: Int = 1,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
    // ------------------------------
    val startupDurationMillis: Long = timeUnit.toMillis(startupDurationUnits),
    val warmupDurationMillis: Long = timeUnit.toMillis(warmupDurationUnits),
    val mainDurationMillis: Long = timeUnit.toMillis(mainDurationUnits),
)
