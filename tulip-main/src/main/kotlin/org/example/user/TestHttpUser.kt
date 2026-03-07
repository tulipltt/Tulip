package org.example.user

// -------------------------------------------------------------------------

import io.github.tulipltt.tulip.api.TulipUtils
import io.github.tulipltt.tulip.user.HttpUser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ThreadLocalRandom

// -------------------------------------------------------------------------

class TestHttpUser : HttpUser() {
    // ----------------------------------------------------------------- //

    override fun onStart(): Boolean {
        if (userId == 0) {
            logger.info("Kotlin")
            super.onStart()
            // not required, just for testing
            val s: String = getActionName(0)
        }
        return true
    }

    // ----------------------------------------------------------------- //

    // 0.25*6 + 0.75*14 = 12.0 ms

    override fun action1(): Boolean {
        // 6 ms delay (average)
        TulipUtils.delayMillisRandom(0, 12)
        return true
    }

    override fun action2(): Boolean {
        // 14 ms delay (average)
        TulipUtils.delayMillisRandom(0, 28)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action3(): Boolean {
        val id: Int = ThreadLocalRandom.current().nextInt(100) + 1
        return httpGet("/posts/{id}", id).isSuccessful
    }

    override fun action4(): Boolean {
        val id: Int = ThreadLocalRandom.current().nextInt(500) + 1
        return httpGet("/comments/{id}", id).isSuccessful
    }

    override fun action5(): Boolean {
        val id: Int = ThreadLocalRandom.current().nextInt(100) + 1
        return httpGet("/albums/{id}", id).isSuccessful
    }

    override fun action6(): Boolean {
        val id: Int = ThreadLocalRandom.current().nextInt(5000) + 1
        return httpGet("/photos/{id}", id).isSuccessful
    }

    override fun action7(): Boolean {
        val id: Int = ThreadLocalRandom.current().nextInt(200) + 1
        return httpGet("/todos/{id}", id).isSuccessful
    }

    // ----------------------------------------------------------------- //

    override fun action8(): Boolean = true

    // ----------------------------------------------------------------- //

    override fun action9(): Boolean = true

    // ----------------------------------------------------------------- //

    override fun action10(): Boolean {
        TulipUtils.delayMillisFixed(10)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun onStop(): Boolean {
        logger.info("  Terminate: UserId = $userId")
        Thread.sleep(100)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun logger(): Logger = logger

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TestHttpUser::class.java)
    }
}

// -------------------------------------------------------------------------
