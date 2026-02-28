package org.example.lib

import io.github.wfouche.tulip.user.HttpUser
import io.javalin.Javalin
import io.javalin.config.JavalinConfig
import io.javalin.http.Context
import io.javalin.http.Handler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer

class TulipLibHttpUserTest {
    val config = HashMap<String, String>()
    val user: HttpUser
    var app: Javalin? = null

    init {
        config["url"] = "http://jsonplaceholder.typicode.com/posts/1"
        config["url"] = "http://localhost:7777"
        config["httpVersion"] = "HTTP_1_1"
        config["connectTimeoutMillis"] = "10000"
        config["readTimeoutMillis"] = "10000"
        user = HttpUser()
        user.initRuntime(0, 0)
        user.initConfig(config)
        user.onStart()
    }

    @BeforeEach
    fun setup() {
        logger().info("Setup before each test")
        app =
            Javalin
                .create(
                    Consumer { config: JavalinConfig? ->
                        config!!
                            .routes
                            // action 1
                            .get(
                                "/posts/{id}",
                                Handler { ctx: Context? ->
                                    ctx!!.status(200)
                                    ctx.result("{\"code\": \"OK\"}").contentType("application/json")
                                },
                            )
                            // action 2
                            .post(
                                "/posts",
                                Handler { ctx: Context? ->
                                    ctx!!.status(201)
                                    ctx.result("{\"code\": \"OK\"}").contentType("application/json")
                                },
                            )
                            // action 3
                            .put(
                                "/posts/{id}",
                                Handler { ctx: Context? ->
                                    ctx!!.status(200)
                                    ctx.result("{\"code\": \"OK\"}").contentType("application/json")
                                },
                            )
                            // action 4
                            .patch(
                                "/posts/{id}",
                                Handler { ctx: Context? ->
                                    ctx!!.status(200)
                                    ctx.result("{\"code\": \"OK\"}").contentType("application/json")
                                },
                            )
                            // action 5
                            .delete(
                                "/posts/{id}",
                                Handler { ctx: Context? ->
                                    ctx!!.status(200)
                                    ctx.result("{\"code\": \"OK\"}").contentType("application/json")
                                },
                            )
                            // action 6
                            .query(
                                "/posts",
                                Handler { ctx: Context? ->
                                    ctx!!.status(200)
                                    ctx.result("{\"code\": \"OK\"}").contentType("application/json")
                                },
                            )
                    },
                ).start(7777)
    }

    @AfterEach
    fun teardown() {
        logger().info("Teardown after each test")
        app?.stop()
        app = null
    }

    // Action 1: GET /posts/{id}
    @Test
    fun action1() {
        logger().info("action1: GET /posts/{id}")
        val id = ThreadLocalRandom.current().nextInt(100) + 1
        val rsp: HttpUser.Response = user.httpGet("/posts/{id}", id)
        if (!rsp.isSuccessful) {
            logger().error("Failed to GET /posts/{}", id)
            assertEquals(0, 1)
        }
        logger().info("GET /posts/{} response: {}", id, rsp)
        assertEquals(0, 0)
    }

    // Action 2: POST /posts
    @Test
    fun action2() {
        logger().info("action2: POST /posts")
        val body = "{\"title\": \"foo\", \"body\": \"bar\", \"userId\": 1}"
        val rsp: HttpUser.Response = user.httpPost(body, "/posts")
        if (!rsp.isSuccessful) {
            logger().error("Failed to POST /posts")
            assertEquals(0, 1)
        }
        logger().info("POST /posts response: {}", rsp)
        assertEquals(0, 0)
    }

    // Action 3: PUT /posts/{id}
    @Test
    fun action3() {
        logger().info("action3: PUT /posts/{id}")
        val id = ThreadLocalRandom.current().nextInt(100) + 1
        val body =
            "{\"id\": " +
                id +
                ", \"title\": \"updated title\"" +
                ", \"body\": \"updated body\", \"userId\": 1}"
        val rsp: HttpUser.Response = user.httpPut(body, "/posts/{id}", id)
        if (!rsp.isSuccessful) {
            logger().error("Failed to PUT /posts/{}", id)
            assertEquals(0, 1)
        }
        logger().info("PUT /posts/{} response: {}", id, rsp)
        assertEquals(0, 0)
    }

    // Action 4: PATCH /posts/{id}
    @Test
    fun action4() {
        logger().info("action4: PATCH /posts/{id}")
        val id = ThreadLocalRandom.current().nextInt(100) + 1
        val body = "{\"title\": \"patched title\"}"
        val rsp: HttpUser.Response = user.httpPatch(body, "/posts/{id}", id)
        if (!rsp.isSuccessful) {
            logger().error("Failed to PATCH /posts/{}", id)
            assertEquals(0, 1)
        }
        logger().info("PATCH /posts/{} response: {}", id, rsp)
        assertEquals(0, 0)
    }

    // Action 5: DELETE /posts/{id}
    @Test
    fun action5() {
        logger().info("action5: DELETE /posts/{id}")
        val id = ThreadLocalRandom.current().nextInt(100) + 1
        val rsp: HttpUser.Response = user.httpDelete("/posts/{id}", id)
        if (!rsp.isSuccessful) {
            logger().error("Failed to DELETE /posts/{}", id)
            assertEquals(0, 1)
        }
        logger().info("DELETE /posts/{} response: {}", id, rsp)
        assertEquals(0, 0)
    }

    // Action 6: QUERY /posts?userId={userId}
    @Test
    fun action6() {
        logger().info("action6: QUERY /posts")
        val body = "{\"title\": \"foo\", \"body\": \"bar\", \"userId\": 1}"
        val rsp: HttpUser.Response = user.httpQuery(body, "/posts")
        if (!rsp.isSuccessful) {
            logger().error("Failed to QUERY /posts")
            assertEquals(0, 1)
        }
        logger().info("QUERY /posts response: {}", rsp)
        assertEquals(0, 0)
    }

    fun logger(): Logger = logger

    // RestClient object
    companion object {
        private val logger = LoggerFactory.getLogger(TulipLibHttpUserTest::class.java)
    }
}
