package io.github.tulipltt.tulip.report

object ReportStyles {
    val grafanaTheme = """
        :root {
            --bg-color: #111217;
            --card-bg: #181b1f;
            --border-color: #2c3235;
            --text-color: #d8d9da;
            --header-color: #313131;
            --accent-color: #3274d9;
            --success-color: #73bf69;
            --error-color: #f2495c;
            --font-family: "Inter", "Helvetica Neue", Arial, sans-serif;
        }

        body {
            background-color: var(--bg-color);
            color: var(--text-color);
            font-family: var(--font-family);
            margin: 0;
            padding: 20px;
        }

        h1, h2, h3 {
            color: var(--text-color);
        }

        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }

        .card {
            background-color: var(--card-bg);
            border: 1px solid var(--border-color);
            border-radius: 4px;
            padding: 15px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.3);
        }

        .card-header {
            font-size: 1.1rem;
            font-weight: bold;
            margin-bottom: 15px;
            border-bottom: 1px solid var(--border-color);
            padding-bottom: 10px;
            display: flex;
            justify-content: space-between;
        }

        .stats-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.9rem;
        }

        .stats-table th {
            text-align: left;
            padding: 8px;
            background-color: var(--header-color);
            border-bottom: 2px solid var(--border-color);
        }

        .stats-table td {
            padding: 8px;
            border-bottom: 1px solid var(--border-color);
        }

        .stats-table tr:hover {
            background-color: rgba(255,255,255,0.05);
        }

        .numeric {
            text-align: right;
            font-family: monospace;
        }

        .status-failed {
            color: var(--error-color);
        }

        .status-success {
            color: var(--success-color);
        }

        .chart-container {
            width: 100%;
            height: 400px;
        }

        .metadata-section {
            margin-bottom: 30px;
            display: flex;
            flex-wrap: wrap;
            gap: 40px;
        }

        .metadata-item {
            display: flex;
            flex-direction: column;
        }

        .metadata-label {
            font-size: 0.8rem;
            color: #8e8e8e;
            text-transform: uppercase;
        }

        .metadata-value {
            font-size: 1.2rem;
            font-weight: bold;
        }
    """.trimIndent()
}
