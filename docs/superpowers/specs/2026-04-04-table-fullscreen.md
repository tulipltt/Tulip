# Design Spec: Unified Table Fullscreen

**Date:** 2026-04-04
**Topic:** Maximize/Fullscreen functionality for data tables
**Goal:** Provide a consistent way to view large data tables in a distraction-free, full-browser mode, matching the existing chart behavior.

## 1. Context & Motivation
Large data tables (Detailed results, LLQ/HDR distributions) can be difficult to read when embedded in long report pages. Users need a way to maximize these tables to fill the browser window for deep analysis.

## 2. Design Goals
- **Consistency:** Use the same interaction pattern and visual language as the chart "fullscreen" mode.
- **Usability:** Ensure sticky headers remain functional in maximized mode.
- **Simplicity:** Reuse existing JavaScript logic where possible.

## 3. Visual Identity (Pico CSS Extended)

### 3.1 Maximize Icon
- **Type:** SVG (stroke-based, matching other icons).
- **Path:** A standard "expand" or "maximize" icon (e.g., Lucide `maximize` or `expand`).
- **Placement:** In the `statsCard` header, next to the CSV/JSON download buttons.

### 3.2 Fullscreen Style Updates
- **Container:** `article.fullscreen` (existing).
- **Table Specifics:** 
  - Ensure `.overflow-auto` containers within `article.fullscreen` expand to fill most of the vertical space (`calc(100vh - 120px)`).
  - Ensure the table `background-color` is consistent.

## 4. Components & Architecture

### 4.1 `ReportComponents.kt`
- Add `ReportIcons.MAXIMIZE` SVG constant.
- Update `statsCard` to include an "Icon only" button using `ReportIcons.MAXIMIZE`.
- This button will call `toggleFullscreen(tableId)` where `tableId` is the ID of the table container (or the `article` itself).

### 4.2 `ReportStyles.kt`
- Add CSS rules to handle the table container inside a fullscreen article:
  ```css
  article.fullscreen .overflow-auto {
      max-height: calc(100vh - 120px) !important;
  }
  ```

### 4.3 `ReportScripts.kt`
- The `toggleFullscreen` function already exists. It finds the closest `article`, toggles the `.fullscreen` class, and handles body overflow.
- It will be updated to accept an ID that points to either a chart or a table.

## 5. Alternatives Considered
- **Browser Fullscreen API:** Rejected due to inconsistent styling and escape-key handling.
- **Dedicated Modal:** Rejected to avoid code duplication; pseudo-fullscreen is already implemented for charts.

## 6. Success Criteria
- Tables have a "Maximize" icon in the header.
- Clicking the icon expands the table to fill the browser window.
- Table headers remain sticky while scrolling in maximized mode.
- Pressing the icon again (or closing the section) restores the original layout.
