# Spec: Migration of Tulip Reporting from Jython to Kotlin

## Status
- **Date**: 2026-03-19
- **Author**: Gemini CLI
- **Status**: Draft (Approved by User)

## 1. Goal
Migrate the existing performance testing report generation from Jython 2.7 (`report.py`) to native Kotlin. The new report should have a professional "Data-Rich" look inspired by Grafana, while maintaining all current metrics and visualizations.

## 2. Success Criteria
- Native Kotlin implementation (no Jython dependency for reporting).
- Single-file static HTML output (portable).
- Professional, dark-themed "Grafana-style" dashboard layout.
- Consistent and accurate reproduction of all existing charts (Percentile Distribution, Time-series for APS/Latency) and tables (HDR, LLQ, Benchmark Summary).
- Migration of the AsciiDoc configuration report (`benchmark_report_c.adoc`) to Kotlin using AsciidoctorJ.
- Improved maintainability through modular Kotlin code (`kotlinx.html` DSL).

## 3. Architecture

### 3.1 Components (Modular Kotlin DSL)
The generator will be split into logical components to ensure clean code and easy extensions.

- **`ReportData`**: Data model to hold the benchmark results (parsed from `report_output.json`).
- **`TulipReportGenerator`**: Orchestrator that parses data and builds the HTML.
- **`ReportComponents`**: Modular functions for:
    - `layout`: Overall HTML structure.
    - `header`: Title, metadata, and navigation.
    - `summaryTable`: High-level benchmark results.
    - `chartsSection`: Container for Google Charts.
    - `hdrTable` / `llqTable`: Detailed distribution tables.
- **`ReportStyles`**: Kotlin object containing the "Grafana-style" CSS.
- **`ReportScripts`**: Kotlin object containing the Google Charts initialization and helper JS logic.

### 3.2 Libraries
- **`kotlinx.html`**: For type-safe HTML generation.
- **`kotlinx.serialization`**: For parsing the input `report_output.json`.
- **`Google Charts`**: For all client-side visualizations (consolidated for consistency).
- **`AsciidoctorJ`**: For generating the AsciiDoc report (already in project).

### 3.3 Data Flow
1. `TulipApi` calls `TulipReportKt.createHtmlReport(outputFilename)`.
2. `TulipReportGenerator` reads and parses `outputFilename` (JSON) using `kotlinx.serialization`.
3. `TulipReportGenerator` uses `kotlinx.html` and `ReportComponents` to build the HTML string.
4. The HTML string is written to the final report file (e.g., `benchmark_report.html`).
5. `TulipReportGenerator` also generates the AsciiDoc report (`benchmark_report_c.adoc`) using a similar data model.

## 4. Visual Design (Grafana Style)
- **Theme**: Dark Mode (Background: `#111217`, Text: `#d8d9da`).
- **Layout**: Grid-based card layout.
- **Cards**: Background: `#181b1f`, Border: `1px solid #2c3235`, Border-radius: `4px`.
- **Tables**: Modern, minimal, with clear headers and subtle row highlights.
- **Charts**: Interactive Google Charts embedded within cards, including:
    - Latency Percentile Distribution.
    - Actions Per Second (APS) over time.
    - Average Latency over time.

## 5. Implementation Plan (High Level)
1. Add `kotlinx-html-jvm` dependency to `tulip-runtime/build.gradle.kts`.
2. Define the `ReportData` models to match `report_output.json` using `@Serializable`.
3. Create `ReportStyles` and `ReportScripts` with the new design and cleaned-up JS.
4. Implement `ReportComponents` using `kotlinx.html`.
5. Implement `TulipReportGenerator` to stitch everything together.
6. Implement AsciiDoc report generation in Kotlin.
7. Update `TulipReportKt.createHtmlReport` to use the new generator instead of Jython.
8. Verify and test against existing `report_output.json`.

## 6. Future-proofing
- The modular design allows easily switching to other charting libraries (like ECharts) in the future if needed.
- Type-safe DSL makes it much harder to introduce broken HTML/JS compared to the previous "string soup" approach.
