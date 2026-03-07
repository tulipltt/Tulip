package io.github.tulipltt.tulip.core

data class TestProfile(
    val enabled: Boolean = true,
    val saveStats: Boolean = true,
    //
    // Name of the benchmark test.
    //
    val name: String = "",
    val duration: Duration = Duration(0, 0, 0, 1),
    // List of actions to be performed.
    // If the weights of all the actions are zero (0), then treat the action
    // list as a workflow to be executed per user object.
    var actions: List<Action>,
    // https://en.wikipedia.org/wiki/Queueing_theory
    //
    // The average arrival rate (arrivals per second) to be maintained.
    //
    val arrivalRate: Double = 0.0,
    val arrivalRateStepChange: Double = 0.0,
    val arrivalRateStepCount: Int = 1,
    // https://en.wikipedia.org/wiki/Little%27s_Law
    //
    // https://www.process.st/littles-law/
    //
    // This value represents the "L" in Little's Law (equation)
    //
    val numTasks: Int = 0,
    // List of percentile values to report on.
    val percentiles: List<Double> = listOf(50.0, 75.0, 90.0, 95.0, 99.0),
    // Json results filename.
    val filename: String = "",
    // (Optional) Name of the workflow to execute
    val workflow: String = "",
)
