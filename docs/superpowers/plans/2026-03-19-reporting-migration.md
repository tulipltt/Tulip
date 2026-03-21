# Migration of Tulip Reporting to Kotlin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate the Jython 2.7 reporting logic (`report.py`) to a native Kotlin generator using `kotlinx.html` with a professional "Data-Rich" Grafana-inspired design.

**Architecture:** Modular Kotlin DSL approach using `kotlinx.html` to build a single-file static HTML report. All charts are consolidated into Google Charts for a consistent, single-file experience. Includes migration of the AsciiDoc configuration report.

**Tech Stack:** Kotlin 2.1+, `kotlinx.html-jvm`, `kotlinx-serialization-json`, Google Charts, AsciidoctorJ.

---

### Task 1: Add Dependencies

**Files:**
- Modify: `tulip-runtime/build.gradle.kts`

- [ ] **Step 1: Add `kotlinx-html-jvm` dependency**
Modify `tulip-runtime/build.gradle.kts` to add:
```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")
```

- [ ] **Step 2: Refresh Gradle**
Run: `./gradlew help` (to trigger a refresh/download).

- [ ] **Step 3: Commit**
```bash
git add tulip-runtime/build.gradle.kts
git commit -m "build: add kotlinx-html-jvm dependency"
```

---

### Task 2: Define Data Model

**Files:**
- Create: `tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/ReportData.kt`
- Test: `tulip-runtime/src/test/kotlin/io/github/tulipltt/tulip/report/ReportDataTest.kt`

- [ ] **Step 1: Write a test for JSON parsing**
Create `tulip-runtime/src/test/kotlin/io/github/tulipltt/tulip/report/ReportDataTest.kt` and add a test that parses a sample `report_output.json`.

- [ ] **Step 2: Define `@Serializable` data classes**
Create `tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/ReportData.kt` with models matching the `report_output.json` structure.

- [ ] **Step 3: Run the test**
Run: `./gradlew :tulip-runtime:test --tests "io.github.tulipltt.tulip.report.ReportDataTest"`
Expected: PASS

- [ ] **Step 4: Commit**
```bash
git add tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/ReportData.kt tulip-runtime/src/test/kotlin/io/github/tulipltt/tulip/report/ReportDataTest.kt
git commit -m "feat: define ReportData models and add parsing tests"
```

---

### Task 3: Create Styles and Scripts

**Files:**
- Create: `tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/ReportStyles.kt`
- Create: `tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/ReportScripts.kt`

- [ ] **Step 1: Create `ReportStyles.kt`**
Add the "Grafana-style" CSS as a multi-line string in a Kotlin object. Include Dark Theme, Grid Layout, and Card styles.

- [ ] **Step 2: Create `ReportScripts.kt`**
Add the Google Charts initialization and helper JS logic as a multi-line string in a Kotlin object.

- [ ] **Step 3: Commit**
```bash
git add tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/ReportStyles.kt tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/ReportScripts.kt
git commit -m "feat: add Grafana-style CSS and Google Charts JS helper objects"
```

---

### Task 4: Implement Report Components

**Files:**
- Create: `tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/ReportComponents.kt`
- Test: `tulip-runtime/src/test/kotlin/io/github/tulipltt/tulip/report/ReportComponentsTest.kt`

- [ ] **Step 1: Write tests for layout components**
Create `tulip-runtime/src/test/kotlin/io/github/tulipltt/tulip/report/ReportComponentsTest.kt` and add tests that verify the generated HTML contains expected CSS classes (e.g., `stats-card`, `grafana-theme`).

- [ ] **Step 2: Implement basic layout components**
Use `kotlinx.html` in `ReportComponents.kt` to create `HTML.reportLayout(data: ReportData, content: DIV.() -> Unit)`.

- [ ] **Step 3: Implement and test table/chart card components**
Create modular functions for `DIV.statsCard(...)`, `TABLE.summaryTable(...)`, etc., and add corresponding tests.

- [ ] **Step 4: Run component tests**
Run: `./gradlew :tulip-runtime:test --tests "io.github.tulipltt.tulip.report.ReportComponentsTest"`
Expected: PASS

- [ ] **Step 5: Commit**
```bash
git add tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/ReportComponents.kt tulip-runtime/src/test/kotlin/io/github/tulipltt/tulip/report/ReportComponentsTest.kt
git commit -m "feat: implement and test modular report components using kotlinx.html"
```

---

### Task 5: Implement HTML Report Generator

**Files:**
- Create: `tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/TulipReportGenerator.kt`
- Modify: `tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/TulipReport.kt`
- Test: `tulip-runtime/src/test/kotlin/io/github/tulipltt/tulip/report/TulipReportGeneratorTest.kt`

- [ ] **Step 1: Write integration test for HTML generation**
Create `tulip-runtime/src/test/kotlin/io/github/tulipltt/tulip/report/TulipReportGeneratorTest.kt`. Verify that `generateHtml` produces a valid HTML string with embedded Google Charts data.

- [ ] **Step 2: Implement `TulipReportGenerator.generateHtml(...)`**
Implement the main orchestration logic that builds the full HTML string.

- [ ] **Step 3: Update `TulipReport.kt`**
Update `createHtmlReport` to call `TulipReportGenerator.generateHtml` and write to file.

- [ ] **Step 4: Run integration tests**
Run: `./gradlew :tulip-runtime:test --tests "io.github.tulipltt.tulip.report.TulipReportGeneratorTest"`
Expected: PASS

- [ ] **Step 5: Commit**
```bash
git add tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/TulipReportGenerator.kt tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/TulipReport.kt tulip-runtime/src/test/kotlin/io/github/tulipltt/tulip/report/TulipReportGeneratorTest.kt
git commit -m "feat: implement HTML report generator and integrate into TulipReport"
```

---

### Task 6: Implement AsciiDoc Report Generator

**Files:**
- Modify: `tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/TulipReportGenerator.kt`
- Modify: `tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/TulipReport.kt`

- [ ] **Step 1: Implement `TulipReportGenerator.generateAsciiDoc(...)`**
Implement logic to generate `benchmark_report_c.adoc` using AsciidoctorJ and the `ReportData` model.

- [ ] **Step 2: Update `TulipReport.kt`**
Ensure `createHtmlReport` (or a related function) also triggers the AsciiDoc report generation.

- [ ] **Step 3: Verify with a full run**
Run the demo app and verify that both the HTML and AsciiDoc reports are generated correctly.
Run: `./run_bench_test.sh`

- [ ] **Step 4: Commit**
```bash
git add tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/TulipReportGenerator.kt tulip-runtime/src/main/kotlin/io/github/tulipltt/tulip/report/TulipReport.kt
git commit -m "feat: implement AsciiDoc report generation in Kotlin"
```

---

### Task 7: Cleanup

- [ ] **Step 1: Remove Jython reporting artifacts**
Remove `reports/report.py` and any related bridge code if no longer needed.
*Wait:* Double-check if `report.py` is used anywhere else before deleting.

- [ ] **Step 2: Commit cleanup**
```bash
git rm reports/report.py
git commit -m "cleanup: remove legacy Jython reporting script"
```
