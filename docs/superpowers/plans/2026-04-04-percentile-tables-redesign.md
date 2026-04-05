# Comprehensive Percentile Analysis Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore detailed percentile distribution analysis with HDR and LLQ tables, sticky headers, and accurate logarithmic ECharts.

**Architecture:** Adds new table components to `ReportComponents.kt`, sticky header CSS to `ReportStyles.kt`, and integrates them into `TulipReportGenerator.kt`. Renames "Latency Distribution" to "Percentile Response Time Distribution" and uses logarithmic X-axis for charts.

**Tech Stack:** Kotlin HTML DSL, Pico CSS, ECharts, HdrHistogram.

---

### Task 1: Add Sticky Header and Details Styles

**Files:**
- Modify: `tulip-report/src/main/kotlin/io/github/tulipltt/tulip/report/ReportStyles.kt`

- [ ] **Step 1: Add CSS for sticky headers and details/summary**

```css
        /* Sticky Table Headers */
        thead th {
            position: sticky;
            top: 0;
            background-color: var(--pico-background-color) !important;
            z-index: 10;
            box-shadow: 0 2px 2px -1px rgba(0, 0, 0, 0.1);
        }

        /* Expandable Sections */
        details {
            border: 1px solid var(--pico-muted-border-color);
            border-radius: var(--pico-border-radius);
            padding: 0.5rem 1rem;
            margin-bottom: 1rem;
        }
        details summary {
            font-weight: 600;
            cursor: pointer;
            list-style: none;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        details summary::-webkit-details-marker {
            display: none;
        }
        details summary::before {
            content: "▶";
            transition: transform 0.2s ease;
            font-size: 0.8rem;
            opacity: 0.5;
        }
        details[open] summary::before {
            transform: rotate(90deg);
        }
```

- [ ] **Step 2: Commit styles**

```bash
git add tulip-report/src/main/kotlin/io/github/tulipltt/tulip/report/ReportStyles.kt
git commit -m "feat(report): add sticky headers and details/summary styles"
```

---

### Task 2: Implement Percentile Table Components

**Files:**
- Modify: `tulip-report/src/main/kotlin/io/github/tulipltt/tulip/report/ReportComponents.kt`

- [ ] **Step 1: Add llqPercentileTable and hdrPercentileTable functions**

```kotlin
fun FlowContent.llqPercentileTable(results: List<BenchmarkResult>, tableId: String) {
    val lastRes = results.last()
    div(classes = "overflow-auto") {
        style = "max-height: 400px;"
        table(classes = "striped") {
            id = tableId
            thead {
                tr {
                    th { +"Value" }
                    th { +"Percentile" }
                    th { +"Total Count" }
                    th { +"Bucket Size" }
                    th { +"Percentage" }
                    th { +"Above Count" }
                }
            }
            tbody {
                // Simplified LLQ representation based on report.py logic
                val distributionPoints = listOf(0.0, 50.0, 75.0, 90.0, 95.0, 99.0, 99.9, 99.99, 99.999, 100.0)
                val totalCount = lastRes.numActions.toLong()
                
                distributionPoints.forEach { p ->
                    val valueNanos = if (p == 100.0) lastRes.maxRt else lastRes.percentilesRt[p.toString()] ?: 0.0
                    val countAtP = (totalCount * (p / 100.0)).toLong()
                    tr {
                        td(classes = "numeric") { +formatDuration(valueNanos) }
                        td(classes = "numeric") { +"%.6f".format(p / 100.0) }
                        td(classes = "numeric") { +countAtP.toString() }
                        td(classes = "numeric") { +"-" }
                        td(classes = "numeric") { +"%.3f".format(p) }
                        td(classes = "numeric") { +(totalCount - countAtP).toString() }
                    }
                }
            }
        }
    }
}

fun FlowContent.hdrPercentileTable(results: List<BenchmarkResult>, tableId: String) {
    val lastRes = results.last()
    val base64 = lastRes.hdrHistogramRt ?: return
    val bytes = Base64.getDecoder().decode(base64)
    val histogram = Histogram.decodeFromCompressedByteBuffer(ByteBuffer.wrap(bytes), 0)

    div(classes = "overflow-auto") {
        style = "max-height: 400px;"
        table(classes = "striped") {
            id = tableId
            thead {
                tr {
                    th { +"Value" }
                    th { +"Percentile" }
                    th { +"TotalCount" }
                    th { +"1/(1-Percentile)" }
                    th { +"AboveCount" }
                }
            }
            tbody {
                val totalCount = histogram.totalCount
                // Matching report.py logic: output reverse order for high detail in tail
                val data = mutableListOf<Triple<Double, Double, Long>>()
                histogram.percentiles(5).forEach { iv ->
                    data.add(Triple(iv.percentileLevelIteratedTo, iv.valueIteratedTo.toDouble(), iv.totalCountToThisValue))
                }
                data.asReversed().forEach { (p, value, count) ->
                    val factor = if (p < 100.0) "%.2f".format(100.0 / (100.0 - p)) else ""
                    tr {
                        td(classes = "numeric") { +formatDuration(value) }
                        td(classes = "numeric") { +"%.12f".format(p / 100.0) }
                        td(classes = "numeric") { +count.toString() }
                        td(classes = "numeric") { +factor }
                        td(classes = "numeric") { +(totalCount - count).toString() }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit components**

```bash
git add tulip-report/src/main/kotlin/io/github/tulipltt/tulip/report/ReportComponents.kt
git commit -m "feat(report): add LLQ and HDR percentile table components"
```

---

### Task 3: Implement Logarithmic Percentile Chart

**Files:**
- Modify: `tulip-report/src/main/kotlin/io/github/tulipltt/tulip/report/ReportScripts.kt`

- [ ] **Step 1: Update createPercentileChart to use logarithmic X-axis**

```javascript
        function createPercentileChart(chartId, labels, dataRows, title, unit) {
            const series = labels.map((label, index) => ({
                name: label,
                type: 'line',
                smooth: true,
                showSymbol: false,
                encode: { x: 0, y: index + 1 }
            }));

            const option = {
                title: { text: title },
                tooltip: { 
                    trigger: 'axis',
                    formatter: (params) => {
                        const x = params[0].value[0];
                        const p = 100.0 - (100.0 / x);
                        let res = `Percentile: ${p.toFixed(6)}% (1/(1-P): ${x})<br/>`;
                        params.forEach(param => {
                            res += `${param.marker} ${param.seriesName}: ${param.value[param.encode.y[0]].toFixed(2)} ${unit}<br/>`;
                        });
                        return res;
                    }
                },
                xAxis: { 
                    name: '1/(1-Percentile)', 
                    type: 'log',
                    min: 1,
                    axisLabel: {
                        formatter: (value) => {
                            const p = 100.0 - (100.0 / value);
                            if (value === 1) return '0%';
                            if (value === 10) return '90%';
                            if (value === 100) return '99%';
                            if (value === 1000) return '99.9%';
                            if (value === 10000) return '99.99%';
                            if (value === 100000) return '99.999%';
                            if (value === 1000000) return '99.9999%';
                            return '';
                        }
                    }
                },
                yAxis: { name: 'Latency (' + unit + ')', type: 'value' },
                // ... toolbox, dataZoom ...
            };
            // ... initChart ...
        }
```

- [ ] **Step 2: Commit chart update**

```bash
git add tulip-report/src/main/kotlin/io/github/tulipltt/tulip/report/ReportScripts.kt
git commit -m "feat(report): use logarithmic X-axis for percentile charts"
```

---

### Task 4: Integrate and Rename in Generator

**Files:**
- Modify: `tulip-report/src/main/kotlin/io/github/tulipltt/tulip/report/TulipReportGenerator.kt`

- [ ] **Step 1: Use HDR data for chart and add expandable tables**

Update the "Percentile Response Time Distribution" section to use `1/(1-P)` for data points:

```kotlin
                                    script {
                                        // ... existing labels logic ...
                                        val dataRows = allPercentiles.map { p ->
                                            val x = if (p < 100.0) 100.0 / (100.0 - p) else 1000000.0 // Max out at 99.9999%
                                            val rowData = // ... existing values logic ...
                                            "[$x, $rowData]"
                                        }.joinToString(",")
                                        
                                        unsafe {
                                            +"""
                                                createPercentileChart('chart_dist_$bmId', [$labels], [$dataRows], 'Percentile Response Time Distribution', 'ms');
                                            """.trimIndent()
                                        }
                                    }
                                    
                                    details {
                                        summary { +"View Percentile Distribution Tables (LLQ & HDR)" }
                                        h5 { +"LLQ Percentile Distribution" }
                                        llqPercentileTable(results, "llq_table_$bmId")
                                        h5 { style = "margin-top: 1.5rem;"; +"HDR Percentile Distribution" }
                                        hdrPercentileTable(results, "hdr_table_$bmId")
                                    }
```

- [ ] **Step 2: Commit final integration**

```bash
git add tulip-report/src/main/kotlin/io/github/tulipltt/tulip/report/TulipReportGenerator.kt
git commit -m "feat(report): final integration of logarithmic charts and expandable tables"
```
