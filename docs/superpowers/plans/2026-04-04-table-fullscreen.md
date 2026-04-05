# Unified Table Fullscreen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a maximize/fullscreen button for data tables, reusing the existing pseudo-fullscreen logic from charts.

**Architecture:** Adds a maximize icon to `ReportIcons`, updates `statsCard` to display it for tables, adds CSS for table expansion, and ensures JS handles the toggle correctly.

**Tech Stack:** Kotlin HTML DSL, Pico CSS, Vanilla JS.

---

### Task 1: Add Maximize Icon and Update statsCard

**Files:**
- Modify: `tulip-report/src/main/kotlin/io/github/tulipltt/tulip/report/ReportComponents.kt`

- [ ] **Step 1: Add MAXIMIZE icon to ReportIcons**

```kotlin
    val THEME = iconBase("""<path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>""")
    val MAXIMIZE = iconBase("""<path d="M15 3h6v6"/><path d="M9 21H3v-6"/><path d="M21 3l-7 7"/><path d="M3 21l7-7"/>""")
}
```

- [ ] **Step 2: Update statsCard to include maximize button for tables**

```kotlin
                if (isTable && tableId != null) {
                    div {
                        style = "text-align: right; display: flex; align-items: center; justify-content: flex-end; gap: 8px;"
                        val titleSlug = titleText.lowercase().replace(" ", "_").replace("/", "_")
                        
                        button(classes = "outline secondary contrast") {
                            style = "padding: 4px 8px; font-size: 0.8em; margin: 0; display: flex; align-items: center;"
                            attributes["onclick"] = "toggleFullscreen('$tableId')"
                            attributes["title"] = "Maximize Table"
                            unsafe { +ReportIcons.MAXIMIZE }
                        }

                        button(classes = "outline secondary contrast") {
                            style = "padding: 4px 8px; font-size: 0.8em; margin: 0;"
                            attributes["onclick"] = "downloadTableAsCSV('$tableId','${titleSlug}.csv')"
                            +"CSV"
                        }
                        button(classes = "outline secondary contrast") {
                            style = "padding: 4px 8px; font-size: 0.8em; margin: 0;"
                            attributes["onclick"] = "downloadTableAsJSON('$tableId','${titleSlug}.json')"
                            +"JSON"
                        }
                    }
                }
```

---

### Task 2: Add CSS for Table Fullscreen Expansion

**Files:**
- Modify: `tulip-report/src/main/kotlin/io/github/tulipltt/tulip/report/ReportStyles.kt`

- [ ] **Step 1: Add CSS for overflow container expansion in fullscreen**

Append to `article.fullscreen` section:

```css
        article.fullscreen .overflow-auto {
            max-height: calc(100vh - 120px) !important;
            border: 1px solid var(--pico-muted-border-color);
        }
```

---

### Task 3: Verify and Refine Fullscreen Script

**Files:**
- Modify: `tulip-report/src/main/kotlin/io/github/tulipltt/tulip/report/ReportScripts.kt`

- [ ] **Step 1: Ensure toggleFullscreen handles table IDs correctly**

(The existing script uses `document.getElementById(id).closest('article')`, which is already generic enough for both chart and table IDs.)

```javascript
        function toggleFullscreen(chartId) {
            const chartDom = document.getElementById(chartId);
            if (!chartDom) return;
            const card = chartDom.closest('article');
            card.classList.toggle('fullscreen');
            const chart = charts.get(chartId);
            if (chart) {
                setTimeout(() => chart.resize(), 310);
            }
            if (card.classList.contains('fullscreen')) {
                document.body.style.overflow = 'hidden';
            } else {
                document.body.style.overflow = 'auto';
            }
        }
```

---

### Task 4: Final Integration Check

- [ ] **Step 1: Build and Verify**

Run: `./gradlew :tulip-report:classes`
Expected: BUILD SUCCESSFUL
