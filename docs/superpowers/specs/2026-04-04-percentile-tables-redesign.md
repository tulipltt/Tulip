# Design Spec: Comprehensive Percentile Response Time Distribution

**Date:** 2026-04-04
**Topic:** Accurate Percentile Charts and Expandable Distribution Tables
**Goal:** Align the new Tulip report with the original's depth of percentile analysis, including HDR and LLQ tables, sticky headers, and high-precision charts.

## 1. Context & Motivation
The user wants to restore the detailed percentile analysis from the original reports. This includes accurate "Percentile Response Time Distribution" charts and detailed, expandable tables for both HDR (High Dynamic Range) and LLQ (Low Latency Quantization) distributions. Tables must have sticky headers for better readability during scrolling.

## 2. Design Goals
- **Consistency:** Restore the naming and depth of analysis from the original report.
- **Accuracy:** Use HDR histogram data directly for charts and tables.
- **Usability:** Implement expandable sections to avoid cluttering the UI, while using sticky headers for long tables.
- **Precision:** Ensure charts correctly reflect the tail latency distribution.

## 3. Visual Identity (Pico CSS Extended)

### 3.1 Sticky Table Headers
- **Style:** `thead tr th { position: sticky; top: 0; background: var(--pico-background-color); z-index: 10; border-bottom: 2px solid var(--pico-muted-border-color); }`
- **Application:** Apply to all data tables in the report.

### 3.2 Expandable Sections
- **Component:** Use HTML5 `<details>` and `<summary>` tags.
- **Style:** `summary { font-weight: 600; cursor: pointer; padding: 0.5rem 0; }`

## 4. Components & Architecture

### 4.1 `ReportComponents.kt`
Add three new functions:
- `llqPercentileTable(results: List<BenchmarkResult>)`: Restore LLQ logic from original report. Columns: `Value`, `Percentile`, `Total Count`, `Bucket Size`, `Percentage`, `Above Count`.
- `hdrPercentileTable(results: List<BenchmarkResult>)`: Use `histogram.outputPercentileDistribution` style logic. Columns: `Value`, `Percentile`, `TotalCount`, `1/(1-Percentile)`, `AboveCount`.
- `expandableSection(title: String, content: FlowContent.() -> Unit)`: Wrapper using `<details>`.

### 4.2 `ReportStyles.kt`
- Add CSS for sticky headers.
- Add CSS for `<details>` and `<summary>` to match Pico CSS aesthetics.

### 4.3 `TulipReportGenerator.kt`
- Rename "Latency Distribution" to "Percentile Response Time Distribution".
- Insert expandable LLQ and HDR tables at the end of each benchmark section.
- **EChart Precision:** The "Percentile Response Time Distribution" chart will use a logarithmic X-axis. The data points will be derived from `1/(1-Percentile)` values (e.g., 1, 10, 100, 1000, 10000) to accurately show tail latency (90%, 99%, 99.9%, etc.), matching the original report's Google Chart implementation.

### 4.4 `ReportScripts.kt`
- (Optional) Add logic to ensure charts resize correctly if they are placed inside `<details>` when opened.

## 5. Alternatives Considered
- **Always Visible Tables:** Rejected to keep the report clean and focus on the summary first.
- **Custom JS Toggle:** Rejected in favor of semantic `<details>`/`<summary>`.

## 6. Success Criteria
- Each benchmark section has a "Percentile Response Time Distribution" chart.
- Each benchmark section has expandable LLQ and HDR tables.
- Table headers remain visible while scrolling through data.
- Data accuracy matches the original report's output.
