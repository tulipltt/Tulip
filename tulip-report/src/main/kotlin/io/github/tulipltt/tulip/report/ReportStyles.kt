package io.github.tulipltt.tulip.report

object ReportStyles {
    fun getStyles(theme: String, mode: String): String {
        return """
        :root {
            --pico-font-family: system-ui, -apple-system, sans-serif;
        }

        /* Sticky Sidebar Layout */
        body {
            display: flex;
            flex-direction: row;
            min-height: 100vh;
            margin: 0;
        }

        aside {
            width: 260px;
            height: 100vh;
            position: fixed;
            overflow-y: auto;
            border-right: 1px solid var(--pico-muted-border-color);
            padding: var(--pico-spacing);
            z-index: 1000;
            transition: transform 0.3s ease;
        }

        aside nav ul {
            flex-direction: column;
            align-items: flex-start;
        }
        aside nav li {
            width: 100%;
            padding: 0;
        }
        aside nav a {
            display: block;
            width: 100%;
            padding: calc(var(--pico-spacing) * 0.5) 0;
        }

        main {            flex: 1;
            margin-left: 260px;
            padding: var(--pico-spacing);
            min-width: 0; /* Prevent flex overflow */
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

        /* Pill styling */
        .pill {
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 0.8em;
            font-weight: bold;
        }
        .pill-fail { background-color: var(--pico-ins-color); color: white; } /* Using Pico red-ish */
        .pill-pass { background-color: var(--pico-primary-background); color: white; }

        @media (max-width: 992px) {
            aside { transform: translateX(-100%); }
            aside.open { transform: translateX(0); }
            main { margin-left: 0; }
        }

        /* Fullscreen handling */
        article.fullscreen {
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw !important;
            height: 100vh !important;
            z-index: 2000;
            margin: 0 !important;
            overflow-y: auto;
        }
        article.fullscreen .chart-container {
            height: calc(100vh - 150px);
        }

        /* Professional Sidebar Enhancements */
        aside {
            background-color: var(--pico-card-background-color);
            padding: 0;
            display: flex;
            flex-direction: column;
        }

        aside header {
            padding: 1.5rem;
            border-bottom: 1px solid var(--pico-muted-border-color);
            margin-bottom: 1rem;
        }

        aside nav {
            padding: 0 0.75rem;
        }

        aside nav ul {
            list-style: none;
            padding: 0;
            margin: 0;
            gap: 4px;
        }

        .nav-section {
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.7rem;
            letter-spacing: 0.05rem;
            opacity: 0.5;
            margin: 1.25rem 0 0.5rem 0.75rem;
        }

        .nav-link {
            display: flex !important;
            align-items: center;
            gap: 0.75rem;
            padding: 0.5rem 0.75rem !important;
            border-radius: 8px;
            color: var(--pico-color);
            text-decoration: none;
            transition: all 0.2s ease;
            font-size: 0.9rem;
        }

        .nav-link:hover {
            background-color: var(--pico-secondary-hover-background);
            color: var(--pico-primary);
        }

        .nav-link.active {
            background-color: var(--pico-primary-background);
            color: var(--pico-primary-inverse);
            font-weight: 600;
        }

        .nav-link.active .nav-icon {
            stroke: var(--pico-primary-inverse);
        }

        .nav-icon {
            opacity: 0.8;
            flex-shrink: 0;
        }
        """.trimIndent()
    }
}
