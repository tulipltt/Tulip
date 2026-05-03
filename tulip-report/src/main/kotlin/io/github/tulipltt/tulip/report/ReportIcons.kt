package io.github.tulipltt.tulip.report

/**
 * SVG icons for the sidebar and UI controls.
 */
object ReportIcons {
    private fun loadIconPath(name: String): String {
        return ReportIcons::class.java.getResource("icons/$name.svg")?.readText()
            ?: error("Icon $name not found")
    }

    private fun iconBase(path: String) =
        """
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" 
             fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" 
             stroke-linejoin="round" class="nav-icon">
            $path
        </svg>
        """.trimIndent()

    val DASHBOARD by lazy { iconBase(loadIconPath("dashboard")) }
    val ACTIVITY by lazy { iconBase(loadIconPath("activity")) }
    val SETTINGS by lazy { iconBase(loadIconPath("settings")) }
    val INFO by lazy { iconBase(loadIconPath("info")) }
    val THEME by lazy { iconBase(loadIconPath("theme")) }
    val MAXIMIZE by lazy { iconBase(loadIconPath("maximize")) }
}
