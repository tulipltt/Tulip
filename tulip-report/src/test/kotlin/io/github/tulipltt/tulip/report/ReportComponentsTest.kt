package io.github.tulipltt.tulip.report

import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReportComponentsTest {

    @Test
    fun `test layout component`() {
        val out = StringBuilder()
        out.appendHTML().html {
            body {
                statsCard("Test Title") {
                    +"Card Content"
                }
            }
        }
        val html = out.toString()
        println("GENERATED HTML: $html")
        assertTrue(html.contains("class=\"card w3-card w3-theme-l5 w3-margin-bottom\""))
        assertTrue(html.contains("class=\"w3-container w3-theme-l2\""))
        assertTrue(html.contains("Test Title"))
        assertTrue(html.contains("Card Content"))
    }
}
