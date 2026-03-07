package io.github.tulipltt.tulip.core

data class Action(
    // Numeric action ID.
    val id: Int,
    // Number of occurrences of this action relative to other actions.
    // Set weight to 0 (or use default value) when a workflow should be
    // specified.
    val weight: Int = 0,
)
