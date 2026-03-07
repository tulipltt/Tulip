// -------------------------------------------------------------------------

package org.example.app

// -------------------------------------------------------------------------

// import io.github.tulipltt.tulip.api.TulipUser
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.github.tulipltt.tulip.api.TulipApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// -------------------------------------------------------------------------

// private class UserFactory00: TulipUserFactory() {
//
//    override fun getUser(userId: Int, className: String, threadId: Int): TulipUser {
//        return when (className) {
//            "user.http.HttpUser" -> org.example.user.HttpUser(userId, threadId)
//            else -> throw Exception("Unknown user class name provided - $className")
//        }
//    }
// }

// -------------------------------------------------------------------------

private class TulipCli00 : CliktCommand() {
    val logger: Logger = LoggerFactory.getLogger(TulipCli00::class.java)
    private val configOpt by option("--config").default("benchmark_config.jsonc")

    override fun run() {
        val outputFilename = TulipApi.runTulip(configOpt)
        TulipApi.generateReport(outputFilename)
    }
}

// -------------------------------------------------------------------------

fun main(args: Array<String>) = TulipCli00().main(args)

// -------------------------------------------------------------------------
