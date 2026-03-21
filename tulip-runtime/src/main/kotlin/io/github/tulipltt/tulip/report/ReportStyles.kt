package io.github.tulipltt.tulip.report

object ReportStyles {
    val grafanaTheme = """
        :root {
            --bg-color: #0b0c10;
            --side-bg: #111217;
            --card-bg: #181b1f;
            --border-color: #2c3235;
            --text-color: #d8d9da;
            --text-muted: #8e8e8e;
            --accent-color: #C735F7;
            --accent-glow: rgba(199, 53, 247, 0.2);
            --success-color: #73bf69;
            --error-color: #FF0087;
            --warning-color: #F8D82E;
            --font-family: 'Inter', system-ui, -apple-system, sans-serif;
        }

        * { box-sizing: border-box; }

        body {
            background-color: var(--bg-color);
            color: var(--text-color);
            font-family: var(--font-family);
            margin: 0;
            display: flex;
            min-height: 100vh;
        }

        .sidebar {
            width: 260px;
            background-color: var(--side-bg);
            border-right: 1px solid var(--border-color);
            padding: 20px;
            position: fixed;
            height: 100vh;
            overflow-y: auto;
            z-index: 100;
        }

        .main-content {
            margin-left: 260px;
            flex: 1;
            padding: 30px;
            max-width: calc(100vw - 260px);
        }

        @media (max-width: 992px) {
            .sidebar { width: 200px; }
            .main-content { margin-left: 200px; max-width: calc(100vw - 200px); }
        }

        @media (max-width: 768px) {
            .sidebar { display: none; }
            .main-content { margin-left: 0; max-width: 100%; padding: 15px; }
        }

        .logo {
            font-size: 1.8rem;
            font-weight: 900;
            background: linear-gradient(135deg, #C735F7 0%, #FF0087 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 40px;
            display: flex;
            align-items: center;
            gap: 12px;
            letter-spacing: -0.02em;
        }

        .nav-group-label {
            font-size: 0.7rem;
            font-weight: 700;
            color: var(--text-muted);
            text-transform: uppercase;
            margin: 25px 0 10px 10px;
            letter-spacing: 0.1em;
        }

        .nav-link {
            display: block;
            padding: 12px 15px;
            color: var(--text-color);
            text-decoration: none;
            border-radius: 6px;
            margin-bottom: 4px;
            font-size: 0.9rem;
            transition: all 0.2s;
            border-left: 3px solid transparent;
        }

        .nav-link:hover {
            background-color: rgba(255,255,255,0.05);
            border-left-color: var(--accent-glow);
        }

        .nav-link.active {
            background-color: rgba(199, 53, 247, 0.15);
            color: var(--accent-color);
            border-left-color: var(--accent-color);
            font-weight: 600;
        }

        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(12, 1fr);
            gap: 24px;
            margin-bottom: 40px;
            margin-top: 24px; /* Space from metadata cards */
        }

        .card {
            background-color: var(--card-bg);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 24px;
            grid-column: span 6;
            transition: transform 0.2s;
        }

        @media (max-width: 1200px) {
            .card { grid-column: span 12; }
        }

        .card.full-width {
            grid-column: span 12;
        }

        .card-header {
            font-size: 0.85rem;
            font-weight: 700;
            color: var(--text-color);
            margin-bottom: 20px;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid var(--border-color);
            padding-bottom: 12px;
        }

        .stats-table-wrapper {
            width: 100%;
            overflow-x: auto; /* Enable scrolling */
            border-radius: 4px;
            margin-top: 10px;
            background: rgba(0,0,0,0.1);
            border: 1px solid var(--border-color);
        }

        .stats-table {
            width: 100%;
            border-collapse: collapse;
            min-width: 1000px; /* Ensure table is wide enough to scroll on mobile */
        }

        .stats-table th {
            text-align: left;
            font-size: 0.7rem;
            color: var(--accent-color);
            text-transform: uppercase;
            padding: 12px 10px;
            border-bottom: 2px solid var(--border-color);
            white-space: nowrap;
            position: sticky;
            top: 0;
            background: var(--card-bg);
        }

        .stats-table td {
            padding: 12px 10px;
            border-bottom: 1px solid var(--border-color);
            font-size: 0.85rem;
        }

        .stats-table tr:last-child td {
            border-bottom: none;
        }

        .numeric {
            text-align: right;
            font-variant-numeric: tabular-nums;
            font-family: 'JetBrains Mono', monospace;
        }

        .status-pill {
            padding: 3px 10px;
            border-radius: 12px;
            font-size: 0.7rem;
            font-weight: 700;
            display: inline-block;
        }

        .status-failed { background: rgba(255, 0, 135, 0.15); color: #FF0087; }
        .status-success { background: rgba(115, 191, 105, 0.15); color: var(--success-color); }

        .chart-container {
            width: 100%;
            height: 380px;
        }

        .metric-card {
            background: var(--card-bg);
            padding: 20px;
            border-top: 3px solid var(--accent-color);
            border-radius: 8px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.2);
        }

        .metric-label { font-size: 0.7rem; color: var(--text-muted); text-transform: uppercase; margin-bottom: 8px; font-weight: 700; }
        .metric-value { font-size: 1.5rem; font-weight: 800; color: #fff; }

        .section-title {
            font-size: 1.5rem;
            font-weight: 800;
            margin: 60px 0 25px 0;
            padding-bottom: 10px;
            border-bottom: 2px solid var(--border-color);
            display: flex;
            align-items: center;
            gap: 15px;
            scroll-margin-top: 30px;
        }

        .section-title::before {
            content: '';
            width: 8px;
            height: 24px;
            background: linear-gradient(to bottom, #C735F7, #FF0087);
            border-radius: 4px;
        }

        .action-group {
            margin-top: 30px;
            background: rgba(255,255,255,0.02);
            padding: 20px;
            border-radius: 8px;
        }
        
        .action-title {
            font-size: 1.1rem;
            font-weight: 700;
            margin-bottom: 15px;
            color: var(--warning-color);
        }
    """.trimIndent()
}
