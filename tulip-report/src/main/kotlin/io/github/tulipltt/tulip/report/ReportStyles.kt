package io.github.tulipltt.tulip.report

object ReportStyles {
    fun getStyles(theme: String, mode: String): String {
        return """
        /* Essential Layout only */
        html, body {
            height: 100%;
            margin: 0;
            font-family: system-ui, -apple-system, sans-serif;
            color: #333;
        }

        .chart-container {
            width: 100%;
            height: 400px;
        }

        .numeric {
            text-align: right;
            font-variant-numeric: tabular-nums;
            font-family: 'JetBrains Mono', monospace;
        }

        .sidebar {
            height: 100%;
            width: 260px;
            position: fixed;
            z-index: 1;
            top: 0;
            left: 0;
            overflow-x: hidden;
            transition: 0.3s;
        }

        .main-content {
            transition: margin-left .3s;
            margin-left: 260px;
        }

        .sidebar.hidden {
            margin-left: -260px;
        }

        .main-content.sidebar-hidden {
            margin-left: 0;
        }

        .sidebar-toggle {
            position: fixed;
            bottom: 20px;
            left: 20px;
            z-index: 2;
        }

        .stats-table-wrapper {
            width: 100%;
            overflow-x: auto;
        }

        /* Fullscreen handling */
        .card.fullscreen {
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw !important;
            height: 100vh !important;
            z-index: 2000;
            margin: 0 !important;
        }
        .card.fullscreen .chart-container {
            height: calc(100vh - 100px);
        }

        /* Dark Mode overrides - ONLY active when data-theme is dark */
        html[data-theme="dark"] {
            background-color: #121212;
            color: #eee;
        }

        html[data-theme="dark"] body {
            background-color: #121212;
            color: #eee;
        }

        html[data-theme="dark"] .w3-theme,
        html[data-theme="dark"] .w3-theme-l1,
        html[data-theme="dark"] .w3-theme-l2,
        html[data-theme="dark"] .w3-theme-l3,
        html[data-theme="dark"] .w3-theme-l4,
        html[data-theme="dark"] .w3-theme-l5,
        html[data-theme="dark"] .w3-theme-d1,
        html[data-theme="dark"] .w3-theme-d2,
        html[data-theme="dark"] .w3-theme-d3,
        html[data-theme="dark"] .w3-theme-d4,
        html[data-theme="dark"] .w3-theme-d5 {
            background-color: #1b1b1b !important;
            color: #eee !important;
        }

        html[data-theme="dark"] .w3-sidebar,
        html[data-theme="dark"] .sidebar,
        html[data-theme="dark"] .main-content,
        html[data-theme="dark"] .card {
            background-color: #1b1b1b !important;
            color: #eee !important;
        }

        html[data-theme="dark"] .w3-table-all { background-color: #222 !important; color: #eee !important; }
        html[data-theme="dark"] .w3-table-all tr { background-color: #222 !important; color: #eee !important; border-bottom: 1px solid #444 !important; }
        html[data-theme="dark"] .w3-table-all tr:nth-child(even) { background-color: #2a2a2a !important; }
        html[data-theme="dark"] .w3-hoverable tbody tr:hover { background-color: #383838 !important; }

        html[data-theme="dark"] .w3-light-grey,
        html[data-theme="dark"] .w3-light-gray,
        html[data-theme="dark"] .w3-white,
        html[data-theme="dark"] .w3-card {
            background-color: #222 !important;
            color: #eee !important;
        }

        /* Ensure table text is readable in light mode */
        .w3-table-all { color: #000; }
        """.trimIndent()
    }
}
