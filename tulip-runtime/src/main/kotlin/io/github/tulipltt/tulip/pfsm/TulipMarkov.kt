package io.github.tulipltt.tulip.pfsm

import io.github.tulipltt.tulip.core.Console
import java.util.concurrent.ThreadLocalRandom
import kotlin.system.exitProcess

data class Edge(val actionId: Int, val weight: Int)

class MarkovChain(val name: String) {
    private val matrix = Array(100, { i -> Array(1000, { j -> 0 }) })

    fun add(actionId: Int, eList: List<Edge>) {
        val pList = mutableListOf<Int>()
        for (e in eList) {
            for (i in 1..e.weight) {
                pList.add(e.actionId)
            }
        }
        pList.shuffle()
        if (pList.size != 1000) {
            Console.put("")
            Console.put("=".repeat(75))
            Console.put("=")
            Console.put("=  TulipMarkovChain: error, probabilities do not sum to 1.0")
            Console.put("=    workflow = $name")
            Console.put("=    actionId = $actionId")
            Console.put("=    list = $eList")
            Console.put("=")
            Console.put("=".repeat(75))
            Console.put("")
            exitProcess(1)
        }
        for (i in 1..1000) {
            matrix[actionId][i - 1] = pList[i - 1]
        }
    }

    fun next(cid: Int): Int {
        val idx = ThreadLocalRandom.current().nextInt(1000)
        return matrix[cid][idx]
    }
}
