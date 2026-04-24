package io.github.tulipltt.tulip.report

/**
 * Provides utilities for sanitizing data to prevent Cross-Site Scripting (XSS).
 */
object XssSanitizer {
    /**
     * Escapes a string for safe use within a JavaScript string literal.
     */
    fun escapeJs(input: String): String {
        return input
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("<", "\\u003c")
            .replace(">", "\\u003e")
            .replace("&", "\\u0026")
    }
}
