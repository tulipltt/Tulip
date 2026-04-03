package io.github.tulipltt.tulip.report

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.File
import kotlinx.serialization.json.Json
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode

object TulipReportGenerator {
    private const val DEFAULT_THEME = "w3-theme-blue-grey" // Change this to switch themes
    private const val DEFAULT_MODE = "dark" // Change this to light if you prefer

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val logoSvg = """<svg width="32" height="32" viewBox="0 0 1388.98 1388.98" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
<g>
	<g>
		<defs><rect id="SVGID_1_" x="663.89" y="1093.34" width="61.19" height="187.33"/></defs>
		<clipPath id="cp1"><use xlink:href="#SVGID_1_" style="overflow:visible;"/></clipPath>
		<rect x="669.07" y="1025.56" style="clip-path:url(#cp1);fill:currentColor;" width="50.83" height="445.27"/>
	</g>
	<path style="fill:none;" d="M694.49,56.69c352.24,0,637.8,285.55,637.8,637.8s-285.55,637.8-637.8,637.8s-637.8-285.55-637.8-637.8S342.24,56.69,694.49,56.69z M108.66,694.49c0,323.54,262.28,585.83,585.83,585.83s585.83-262.28,585.83-585.83s-262.28-585.83-585.83-585.83S108.66,370.94,108.66,694.49z"/>
	<path style="fill:currentColor;" d="M1332.28,694.49c0,352.24-285.55,637.8-637.8,637.8s-637.8-285.55-637.8-637.8s285.55-637.8,637.8-637.8S1332.28,342.24,1332.28,694.49z M694.49,108.66c-323.54,0-585.83,262.28-585.83,585.83s262.28,585.83,585.83,585.83s585.83-262.28,585.83-585.83S1018.03,108.66,694.49,108.66z"/>
	<path style="fill:currentColor;" d="M884.47,434.38c41.01,71.05,64.48,153.37,64.48,241.03s-23.47,169.98-64.48,241.03c-42.46,73.55-103.65,134.84-177.02,177.28l-12.84,3.19l-13.08-3.19c-73.37-42.44-134.56-103.72-177.02-177.28c-41.01-71.05-64.48-153.37-64.48-241.03s23.47-169.98,64.48-241.03c42.46-73.55,103.65-134.84,177.02-177.28l12.96-7.5l12.96,7.5C780.81,299.54,842.01,360.83,884.47,434.38 M897.11,675.41c0-78.55-20.89-152.07-57.39-215.31c-35.32-61.19-85.32-112.93-145.23-150.44c-59.9,37.51-109.91,89.24-145.23,150.44c-36.5,63.24-57.39,136.76-57.39,215.31s20.89,152.07,57.39,215.31c35.32,61.19,85.32,112.93,145.23,150.44c59.9-37.51,109.91-89.24,145.23-150.44C876.22,827.48,897.11,753.95,897.11,675.41z"/>
	<g>
		<defs><path id="SVGID_2_" d="M821.02,455.52c-59.74,42.13-109.56,100.06-142.45,171.21c-32.93,71.24-44.81,146.74-38.21,219.52c6.39,70.46,30.11,138.48,68.81,197.71c314.48-34.13,349.64-209.29,332.88-392.12c-4.32-47.11-12.03-95.46-19.49-142.19c-7.13-44.71-14.04-87.96-17.71-128.3C938.89,391.49,875.82,416.87,821.02,455.52 M1053.1,324.24l1.39,26.85c2.35,45.48,10.56,96.89,19.1,150.45c7.56,47.37,15.38,96.38,19.89,145.63c19.46,212.32-26.12,415.55-399,449.72l-14.43-2.3l-4.84-8.53c-48.79-69.31-78.62-150.61-86.29-235.16c-7.41-81.65,5.85-166.22,42.66-245.84c36.85-79.71,92.69-144.63,159.66-191.87c69.26-48.85,150.53-78.75,235.08-86.5L1053.1,324.24z"/></defs>
		<clipPath id="cp2"><use xlink:href="#SVGID_2_" style="overflow:visible;"/></clipPath>
		<rect x="581.52" y="324.24" style="clip-path:url(#cp2);fill:currentColor;" width="531.42" height="772.65"/>
	</g>
	<g>
		<defs><path id="SVGID_3_" d="M567.95,455.52c-54.8-38.65-117.87-64.03-183.83-74.18c-3.67,40.34-10.58,83.6-17.71,128.3c-7.46,46.73-15.17,95.07-19.49,142.19c-16.76,182.83,18.4,357.99,332.88,392.12c38.7-59.22,62.42-127.24,68.81-197.71c6.6-72.78-5.28-148.28-38.21-219.52C677.51,555.58,627.69,497.65,567.95,455.52 M362.64,326.7c84.55,7.75,165.82,37.65,235.08,86.5c66.97,47.23,122.81,112.15,159.66,191.87c36.81,79.62,50.07,164.19,42.66,245.84c-7.67,84.55-37.5,165.85-86.29,235.16l-4.84,8.53l-14.43,2.3c-372.88-34.18-418.46-237.41-399-449.72c4.51-49.25,12.34-98.26,19.89-145.63c8.55-53.55,16.75-104.97,19.1-150.45l1.39-26.85L362.64,326.7z"/></defs>
		<clipPath id="cp3"><use xlink:href="#SVGID_3_" style="overflow:visible;"/></clipPath>
		<rect x="276.03" y="324.24" style="clip-path:url(#cp3);fill:currentColor;" width="531.42" height="772.65"/>
	</g>
</g>
</svg>"""

    fun generateHtml(reportData: ReportData): String {
        val out = StringBuilder()
        out.append("<!DOCTYPE html>\n")
        out.appendHTML().html {
            lang = "en"
            // Use internal default theme/mode values; ReportData does not carry styling fields.
            val theme = DEFAULT_THEME
            val mode = DEFAULT_MODE
            val isDark = mode == "dark"

            if (mode == "light" || mode == "dark") {
                attributes["data-theme"] = mode
            }
            head {
                meta { charset = "UTF-8" }
                title { +"Tulip Performance Report" }
                link(rel = "stylesheet", href = "https://www.w3schools.com/w3css/4/w3.css")
                link(rel = "stylesheet", href = "https://www.w3schools.com/lib/${theme}.css")
                style { unsafe { +ReportStyles.getStyles(theme, mode) } }
                script { src = "https://cdn.jsdelivr.net/npm/echarts@5.5.0/dist/echarts.min.js" }
                script { unsafe { +ReportScripts.getScripts(theme, mode) } }
            }
            body {
                val groupedResults = reportData.results.groupBy { it.bmName }
                val bodyClass = if (isDark) "w3-theme-d5" else "w3-light-grey"
                classes = setOf(bodyClass)
                
                button(classes = "sidebar-toggle w3-button w3-theme w3-xlarge w3-hide-large") {
                    attributes["onclick"] = "toggleSidebar()"
                    +"☰"
                }

                val sidebarClass = if (isDark) "w3-theme-d5" else "w3-theme-l5"
                val sidebarHeaderClass = if (isDark) "w3-theme-d2" else "w3-theme"

                div("sidebar w3-sidebar w3-bar-block w3-collapse $sidebarClass") {
                    id = "mySidebar"
                    div("w3-container $sidebarHeaderClass") {
                        div("w3-padding-16 w3-center") {
                            unsafe { +logoSvg }
                            h3 { +"Tulip" }
                        }
                    }
                    nav {
                        div("w3-bar-item w3-small w3-opacity") { +"Overview" }
                        a(href = "#overview", classes = "nav-link w3-bar-item w3-button active") { +"Summary Dashboard" }
                        
                        div("w3-bar-item w3-small w3-opacity w3-margin-top") { +"Benchmarks" }
                        groupedResults.keys.forEach { name ->
                            a(href = "#benchmark_${name.replace(" ", "_")}", classes = "nav-link w3-bar-item w3-button") { +name }
                        }

                        div("w3-bar-item w3-small w3-opacity w3-margin-top") { +"Configuration" }
                        a(href = "#config", classes = "nav-link w3-bar-item w3-button") { +"Benchmark Config" }
                        a(href = "#runtime", classes = "nav-link w3-bar-item w3-button") { +"Tulip Runtime" }

                        div("w3-bar-item w3-small w3-opacity w3-margin-top") { +"Appearance" }
                        a(href = "javascript:void(0)", classes = "nav-link w3-bar-item w3-button") {
                            attributes["onclick"] = "toggleTheme()"
                            +"🌓 Toggle Theme"
                        }
                    }
                }

                div("main-content w3-main") {
                    val headerClass = if (isDark) "w3-theme-d1" else "w3-theme"
                    header("w3-container $headerClass") {
                        div("w3-cell-row w3-padding-24") {
                            div("w3-cell w3-cell-middle w3-text-theme") {
                                style = "width: 80px"
                                unsafe { 
                                    val largeLogo = logoSvg.replace("width=\"32\" height=\"32\"", "width=\"64\" height=\"64\"")
                                    +largeLogo 
                                }
                            }
                            div("w3-cell w3-cell-middle") {
                                h2 { +"Performance Test Results" }
                                div("w3-opacity w3-small") {
                                    +"Tulip Performance Tool • Version ${reportData.version}"
                                }
                            }
                        }
                        
                        div("w3-row-padding w3-margin-bottom") {
                            metricCard("Timestamp", reportData.timestamp, isDark)
                            metricCard("Benchmarks", groupedResults.size.toString(), isDark)
                            metricCard("Total Actions", reportData.results.sumOf { it.numActions.toLong() }.toString(), isDark)
                            val totalFailed = reportData.results.sumOf { it.numFailed.toLong() }
                            metricCard("Total Failed", totalFailed.toString(), isDark)
                        }
                    }

                    div("w3-container") {
                        div {
                            id = "overview"
                            statsCard("All Benchmarks Summary", isDark = isDark, classes = "full-width", isTable = true, tableId = "summary_table") {
                                summaryTable(groupedResults, isDark, "summary_table")
                            }

                            val maxRows = groupedResults.values.maxOfOrNull { it.size } ?: 0

                            statsCard("Throughput Comparison (All Actions)", isDark = isDark, classes = "full-width", isChart = true) {
                                div("chart-container") { id = "chart_combined_aps" }
                                script {
                                    val seriesLabels = mutableListOf<String>()
                                    groupedResults.forEach { (bmName, results) ->
                                        val actionNames = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                                        actionNames.forEach { actionName ->
                                            seriesLabels.add("'$bmName - $actionName'")
                                            seriesLabels.add("'$bmName - $actionName (Errors)'")
                                        }
                                    }
                                    val labels = seriesLabels.joinToString(",")
                                    
                                    val dataRows = (0 until maxRows).map { rowIdx ->
                                        val rowData = mutableListOf<String>()
                                        groupedResults.forEach { (_, results) ->
                                            val res = results.getOrNull(rowIdx)
                                            val actionNames = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                                            actionNames.forEach { actionName ->
                                                val actionStats = res?.userActions?.values?.find { it.name == actionName }
                                                val aps = actionStats?.avgAps ?: 0.0
                                                val errAps = if (actionStats != null && res.duration > 0) actionStats.numFailed.toDouble() / res.duration else 0.0
                                                rowData.add(aps.toString())
                                                rowData.add(errAps.toString())
                                            }
                                        }
                                        "[$rowIdx, ${rowData.joinToString(",")}]"
                                    }.joinToString(",")
                                    
                                    unsafe {
                                        +"""
                                            createTimeSeriesChart('chart_combined_aps', [$labels], [$dataRows], 'Throughput per Action (All Benchmarks)', 'APS');
                                        """.trimIndent()
                                    }
                                }
                            }

                            statsCard("Latency Comparison (All Actions)", isDark = isDark, classes = "full-width", isChart = true) {
                                div("chart-container") { id = "chart_combined_rt" }
                                script {
                                    val seriesLabels = mutableListOf<String>()
                                    groupedResults.forEach { (bmName, results) ->
                                        val actionNames = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                                        actionNames.forEach { actionName ->
                                            seriesLabels.add("'$bmName - $actionName'")
                                        }
                                    }
                                    val labels = seriesLabels.joinToString(",")

                                    val dataRows = (0 until maxRows).map { rowIdx ->
                                        val rowData = mutableListOf<String>()
                                        groupedResults.forEach { (_, results) ->
                                            val res = results.getOrNull(rowIdx)
                                            val actionNames = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                                            actionNames.forEach { actionName ->
                                                val actionStats = res?.userActions?.values?.find { it.name == actionName }
                                                val rt = if (actionStats != null) (actionStats.avgRt / 1_000_000.0) else "null"
                                                rowData.add(rt.toString())
                                            }
                                        }
                                        "[$rowIdx, ${rowData.joinToString(",")}]"
                                    }.joinToString(",")
                                    
                                    unsafe {
                                        +"""
                                            createTimeSeriesChart('chart_combined_rt', [$labels], [$dataRows], 'Average Latency per Action (All Benchmarks)', 'ms');
                                        """.trimIndent()
                                    }
                                }
                            }

                            statsCard("Latency Distribution Comparison (All Actions)", isDark = isDark, classes = "full-width", isChart = true) {
                                div("chart-container") { id = "chart_combined_dist" }
                                script {
                                    val seriesLabels = mutableListOf<String>()
                                    groupedResults.forEach { (bmName, results) ->
                                        val actionNames = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                                        actionNames.forEach { actionName ->
                                            seriesLabels.add("'$bmName - $actionName'")
                                        }
                                    }
                                    val labels = seriesLabels.joinToString(",")

                                    val allPercentiles = reportData.results
                                        .flatMap { it.percentilesRt.keys }
                                        .filter { it.toDoubleOrNull() != null }
                                        .map { it.toDouble() }
                                        .filter { it >= 50.0 }
                                        .distinct()
                                        .sorted()

                                    val dataRows = allPercentiles.map { p ->
                                        val rowData = mutableListOf<String>()
                                        groupedResults.forEach { (_, results) ->
                                            val lastRes = results.last()
                                            val actionNames = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()
                                            actionNames.forEach { actionName ->
                                                val actionStats = lastRes.userActions.values.find { it.name == actionName }
                                                val valNanos = if (p == 100.0) {
                                                    actionStats?.maxRt
                                                } else {
                                                    actionStats?.percentilesRt?.get(p.toString()) ?: 
                                                    actionStats?.percentilesRt?.get(p.toInt().toString() + ".0")
                                                }
                                                val valMs = if (valNanos != null) (valNanos / 1_000_000.0) else "null"
                                                rowData.add(valMs.toString())
                                            }
                                        }
                                        "[$p, ${rowData.joinToString(",")}]"
                                    }.joinToString(",")

                                    unsafe {
                                        +"""
                                            createPercentileChart('chart_combined_dist', [$labels], [$dataRows], 'Tail Latency Comparison (All Actions)', 'ms');
                                        """.trimIndent()
                                    }
                                }
                            }
                        }

                        // Detailed sections for each benchmark
                        groupedResults.forEach { (bmName, results) ->
                            val bmId = bmName.replace(" ", "_")
                            h3("w3-container w3-theme-l1 w3-padding w3-margin-top") {
                                id = "benchmark_$bmId"
                                +bmName 
                            }
                            
                            div {
                                statsCard("Action Results", isDark = isDark, classes = "full-width", isTable = true, tableId = "detail_${bmId}_table") {
                                    detailedBenchmarkTable(results, "detail_${bmId}_table")
                                }

                                val allActions = results.flatMap { it.userActions.values.map { a -> a.name ?: "" } }.distinct().sorted()

                                statsCard("Latency Distribution per Action", isDark = isDark, classes = "full-width", isChart = true) {
                                    div("chart-container") { id = "chart_dist_$bmId" }
                                    script {
                                        val lastResult = results.last()
                                        val labels = allActions.joinToString(",") { "'$it'" }
                                        
                                        val allPercentiles = lastResult.userActions.values
                                            .flatMap { it.percentilesRt.keys }
                                            .filter { it.toDoubleOrNull() != null }
                                            .map { it.toDouble() }
                                            .filter { it >= 50.0 }
                                            .distinct()
                                            .sorted()

                                        val dataPoints = allPercentiles.map { p ->
                                            val rowData = allActions.map { actionName ->
                                                val actionStats = lastResult.userActions.values.find { it.name == actionName }
                                                val valNanos = if (p == 100.0) {
                                                    actionStats?.maxRt
                                                } else {
                                                    actionStats?.percentilesRt?.get(p.toString()) ?: 
                                                    actionStats?.percentilesRt?.get(p.toInt().toString() + ".0")
                                                }
                                                if (valNanos != null) (valNanos / 1_000_000.0) else "null"
                                            }.joinToString(",")
                                            "[$p, $rowData]"
                                        }.joinToString(",")
                                        
                                        unsafe {
                                            +"""
                                                createPercentileChart('chart_dist_$bmId', [$labels], [$dataPoints], 'Tail Latency per Action (ms)', 'ms');
                                            """.trimIndent()
                                        }
                                    }
                                }

                                statsCard("Throughput (APS) per Action", isDark = isDark, classes = "full-width", isChart = true) {
                                    div("chart-container") { id = "chart_aps_$bmId" }
                                    script {
                                        val labels = allActions.flatMap { listOf("'$it'", "'$it (Errors)'") }.joinToString(",")
                                        
                                        val dataRows = results.map { res ->
                                            val rowData = allActions.map { actionName ->
                                                val actionStats = res.userActions.values.find { it.name == actionName }
                                                val aps = actionStats?.avgAps ?: 0.0
                                                val errAps = if (actionStats != null && res.duration > 0) actionStats.numFailed.toDouble() / res.duration else 0.0
                                                "$aps, $errAps"
                                            }.joinToString(",")
                                            "[${res.rowId}, $rowData]"
                                        }.joinToString(",")

                                        unsafe {
                                            +"""
                                                createTimeSeriesChart('chart_aps_$bmId', [$labels], [$dataRows], 'Throughput per Action', 'APS');
                                            """.trimIndent()
                                        }
                                    }
                                }

                                statsCard("Average Latency per Action", isDark = isDark, classes = "full-width", isChart = true) {
                                    div("chart-container") { id = "chart_rt_$bmId" }
                                    script {
                                        val labels = allActions.joinToString(",") { "'$it'" }
                                        val dataRows = results.map { res ->
                                            val rowData = allActions.map { actionName ->
                                                val actionStats = res.userActions.values.find { it.name == actionName }
                                                if (actionStats != null) (actionStats.avgRt / 1_000_000.0) else "null"
                                            }.joinToString(",")
                                            "[${res.rowId}, $rowData]"
                                        }.joinToString(",")

                                        unsafe {
                                            +"""
                                                createTimeSeriesChart('chart_rt_$bmId', [$labels], [$dataRows], 'Avg Latency per Action (ms)', 'ms');
                                            """.trimIndent()
                                        }
                                    }
                                }
                            }
                        }

                        // Benchmark Configuration Section
                        h3("w3-container w3-theme-l1 w3-padding w3-margin-top") {
                            id = "config"
                            +"Benchmark Configuration"
                        }
                        div {
                            configSection(reportData.config, isDark)
                        }

                        // Tulip Runtime Information Section
                        h3("w3-container w3-theme-l1 w3-padding w3-margin-top") {
                            id = "runtime"
                            +"Tulip Runtime"
                        }
                        div {
                            runtimeSection(reportData, isDark)
                        }
                    }
                }
            }
        }
        return out.toString()
    }

    private fun FlowContent.metricCard(label: String, value: String, isDark: Boolean) {
        div("w3-quarter w3-margin-bottom") {
            val cardClass = if (isDark) "w3-theme-d4" else "w3-white"
            div("w3-container w3-card $cardClass") {
                h6("w3-opacity w3-small w3-margin-top") { +label }
                p("w3-large") { +value }
            }
        }
    }

    fun generateAsciiDoc(reportData: ReportData): String {
        val adoc = StringBuilder()
        adoc.append("= Benchmark Configuration Report\n")
        adoc.append(":toc: left\n\n")
        
        adoc.append("== Metadata\n\n")
        adoc.append("* *Version*: ${reportData.version}\n")
        adoc.append("* *Timestamp*: ${reportData.timestamp}\n\n")
        
        adoc.append("== Actions\n\n")
        val actions = reportData.config.actions
        adoc.append("[cols=\"1,2\"]\n|===\n")
        adoc.append("| Description | ${actions.description}\n")
        adoc.append("| User Class | ${actions.userClass}\n")
        adoc.append("| Output File | ${actions.jsonFilename}\n")
        adoc.append("| Report File | ${actions.htmlFilename}\n")
        adoc.append("|===\n\n")
        
        adoc.append("=== User Parameters\n\n")
        adoc.append("[cols=\"1,2\"]\n|===\n")
        actions.userParams.forEach { (k, v) ->
            adoc.append("| $k | $v\n")
        }
        adoc.append("|===\n\n")
        
        adoc.append("== Benchmarks\n\n")
        reportData.config.benchmarks.forEach { (name, config) ->
            adoc.append("=== $name\n\n")
            adoc.append("* *Enabled*: ${config.enabled}\n")
            adoc.append("* *APS Rate*: ${config.throughputRate}\n")
            adoc.append("* *Workflow*: ${config.workflow}\n")
            adoc.append("\n")
        }
        
        return adoc.toString()
    }

    fun convertAdocToHtml(adocPath: String) {
        val asciidoctor = Asciidoctor.Factory.create()
        val stylesheetUrl = "https://raw.githubusercontent.com/tulipltt/Tulip/refs/heads/main/docs/css/adoc-foundation.css"
        val attributes = Attributes.builder()
            .attribute("linkcss", false)
            .attribute("data-uri", true)
            .attribute("allow-uri-read", true)
            .attribute("stylesheet", stylesheetUrl)
            .build()
        
        try {
            asciidoctor.requireLibrary("asciidoctor-diagram")
        } catch (e: Exception) {
            println("Warning: asciidoctor-diagram not found")
        }

        asciidoctor.convertFile(
            File(adocPath),
            Options.builder()
                .toFile(true)
                .attributes(attributes)
                .safe(SafeMode.UNSAFE)
                .build()
        )
        asciidoctor.shutdown()
    }

    fun createReport(jsonPath: String, htmlOutputPath: String) {
        val jsonText = File(jsonPath).readText()
        val reportData = json.decodeFromString<ReportData>(jsonText)
        
        // Generate HTML Report
        val html = generateHtml(reportData)
        File(htmlOutputPath).writeText(html)
        
        // Generate AsciiDoc Report
        val adoc = generateAsciiDoc(reportData)
        val adocPath = htmlOutputPath.replace(".html", "_c.adoc")
        File(adocPath).writeText(adoc)
        
        // Convert AsciiDoc to HTML
        convertAdocToHtml(adocPath)
    }
}
