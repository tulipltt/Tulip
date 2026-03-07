package io.github.tulipltt.tulip.core

import kotlinx.serialization.json.JsonPrimitive

data class RuntimeContext(
    val name: String = "",
    val numUsers: Int = 0,
    val numTasks: Int = 0,
    val numThreads: Int = 0,
    val userParams: Map<String, JsonPrimitive> = mapOf(),
)
