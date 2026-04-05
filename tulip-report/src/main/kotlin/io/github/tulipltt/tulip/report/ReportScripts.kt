package io.github.tulipltt.tulip.report

object ReportScripts {
    fun getScripts(theme: String, mode: String): String {
        return """
        let echartsTheme = {};
        const charts = new Map();

        // Apply saved theme immediately
        const savedTheme = localStorage.getItem('tulip-theme');
        if (savedTheme) {
            document.documentElement.setAttribute('data-theme', savedTheme);
        }

        function probeColors() {
            const style = getComputedStyle(document.documentElement);
            
            const getColor = (name, fallback) => {
                const val = style.getPropertyValue(name).trim();
                return val || fallback;
            };

            // Professional palette using standard colors that work in light/dark
            const palette = [
                '#3b82f6', // blue
                '#ef4444', // red
                '#22c55e', // green
                '#f59e0b', // amber
                '#8b5cf6', // purple
                '#06b6d4', // cyan
                '#ec4899', // pink
                '#f97316', // orange
                '#14b8a6', // teal
                '#64748b'  // slate
            ];

            const bg = getColor('--pico-background-color', '#fff');
            const text = getColor('--pico-color', '#000');
            const muted = getColor('--pico-muted-color', '#666');
            const accent = getColor('--pico-primary', '#3b82f6');
            const border = getColor('--pico-muted-border-color', '#ddd');

            return { palette, bg, text, muted, border, accent };
        }

        function updateEchartsTheme() {
            const colors = probeColors();
            
            echartsTheme = {
                backgroundColor: 'transparent', // Let CSS handle background
                textStyle: { color: colors.text, fontFamily: 'system-ui, sans-serif' },
                title: { textStyle: { color: colors.text, fontSize: 14, fontWeight: 'bold' } },
                legend: { 
                    textStyle: { color: colors.text, fontSize: 11 },
                    pageTextStyle: { color: colors.text },
                    orient: 'vertical',
                    right: 10,
                    top: 'middle',
                    type: 'scroll'
                },
                tooltip: { 
                    backgroundColor: colors.bg,
                    borderColor: colors.accent,
                    textStyle: { color: colors.text, fontSize: 12 },
                    confine: true
                },
                grid: { left: '8%', right: '22%', bottom: '15%', top: '15%', containLabel: true },
                categoryAxis: {
                    axisLine: { lineStyle: { color: colors.text, opacity: 0.3 } },
                    axisLabel: { color: colors.text, fontSize: 10, opacity: 0.7 },
                    splitLine: { show: false }
                },
                valueAxis: {
                    axisLine: { lineStyle: { color: colors.text, opacity: 0.3 } },
                    axisLabel: { color: colors.text, fontSize: 10, opacity: 0.7 },
                    splitLine: { lineStyle: { color: colors.text, type: 'dashed', opacity: 0.1 } }
                },
                color: colors.palette
            };

            // Update all existing charts with the new theme
            charts.forEach((chart, id) => {
                chart.setOption(echartsTheme);
            });
        }

        function toggleTheme() {
            const current = document.documentElement.getAttribute('data-theme');
            let next = 'dark';
            
            if (current === 'dark') {
                next = 'light';
            } else if (current === 'light') {
                next = 'dark';
            } else {
                const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                next = prefersDark ? 'dark' : 'light';
            }
            
            document.documentElement.setAttribute('data-theme', next);
            localStorage.setItem('tulip-theme', next);
            location.reload(); 
        }

        function updateActiveLink() {
            const hash = window.location.hash || '#overview';
            document.querySelectorAll('.nav-link').forEach(link => {
                const linkHash = link.getAttribute('href');
                if (linkHash && linkHash.startsWith('#')) {
                    link.classList.toggle('active', linkHash === hash);
                }
            });
        }
        window.addEventListener('hashchange', updateActiveLink);
        window.addEventListener('DOMContentLoaded', updateActiveLink);

        function initChart(chartId, option) {
            const chartDom = document.getElementById(chartId);
            if (!chartDom) return;
            
            if (!Object.keys(echartsTheme).length) {
                updateEchartsTheme();
            }

            const mergedOption = { ...echartsTheme, ...option };
            
            // Explicitly merge sub-objects to prevent theme styles from being overwritten by partial options
            if (echartsTheme.title && option.title) {
                mergedOption.title = { ...echartsTheme.title, ...option.title };
            }
            if (echartsTheme.legend && option.legend) {
                mergedOption.legend = { ...echartsTheme.legend, ...option.legend };
            }
            if (echartsTheme.tooltip && option.tooltip) {
                mergedOption.tooltip = { ...echartsTheme.tooltip, ...option.tooltip };
            }
            
            // Handle axis merging - map theme's category/value axis styles to option's xAxis/yAxis
            if (option.xAxis) {
                const axisStyle = option.xAxis.type === 'category' ? echartsTheme.categoryAxis : echartsTheme.valueAxis;
                mergedOption.xAxis = { ...axisStyle, ...option.xAxis };
            }
            if (option.yAxis) {
                const axisStyle = option.yAxis.type === 'category' ? echartsTheme.categoryAxis : echartsTheme.valueAxis;
                mergedOption.yAxis = { ...axisStyle, ...option.yAxis };
            }

            const myChart = echarts.init(chartDom);
            myChart._chartId = chartId;
            myChart.setOption(mergedOption, true); // Use notMerge=true to ensure clean state
            charts.set(chartId, myChart);
            return myChart;
        }

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
                        let pText = "";
                        if (x >= 1000000000) {
                            pText = "100% (Max)";
                        } else {
                            const p = 100.0 - (100.0 / x);
                            pText = `${'$'}{p.toFixed(6)}%`;
                        }
                        let res = `Percentile: ${'$'}{pText} (1/(1-P): ${'$'}{x.toFixed(2)})<br/>`;
                        params.forEach(param => {
                            const val = param.value[param.encode.y[0]];
                            const valText = (val !== null && val !== undefined) ? val.toFixed(2) : "N/A";
                            res += `${'$'}{param.marker} ${'$'}{param.seriesName}: ${'$'}{valText} ${'$'}{unit}<br/>`;
                        });
                        return res;
                    }
                },
                xAxis: { 
                    name: '1/(1-Percentile)', 
                    type: 'log',
                    min: 1,
                    max: 1000000000,
                    axisLabel: {
                        formatter: (value) => {
                            if (value === 1) return '0%';
                            if (value === 10) return '90%';
                            if (value === 100) return '99%';
                            if (value === 1000) return '99.9%';
                            if (value === 10000) return '99.99%';
                            if (value === 100000) return '99.999%';
                            if (value === 1000000) return '99.9999%';
                            if (value === 10000000) return '99.99999%';
                            if (value === 100000000) return '99.999999%';
                            if (value === 1000000000) return 'Max';
                            return '';
                        }
                    }
                },
                yAxis: { name: 'Latency (' + unit + ')', type: 'value' },
                toolbox: {
                    show: true,
                    feature: {
                        myFullscreen: {
                            show: true,
                            title: 'Fullscreen',
                            icon: 'path://M432.45,595.444c0,2.177-4.661,6.82-11.308,6.82c-6.648,0-11.309-4.643-11.309-6.82s4.661-6.82,11.309-6.82C427.789,588.624,432.45,593.267,432.45,595.444L432.45,595.444zM586.767,666.258c7.656,0,13.854,6.337,13.854,14.141c0,7.804-6.198,14.141-13.854,14.141c-7.664,0-13.854-6.337-13.854-14.141C572.913,672.595,579.103,666.258,586.767,666.258L586.767,666.258zM568.629,667.071c-7.654,0-13.854,6.198-13.854,13.854c0,7.664,6.2,13.854,13.854,13.854c7.664,0,13.854-6.19,13.854-13.854C582.483,673.269,576.293,667.071,568.629,667.071L568.629,667.071zM356.99,505.941c0-2.177,4.661-6.82,11.309-6.82c6.648,0,11.308,4.643,11.308,6.82s-4.66,6.82-11.308,6.82C361.651,512.761,356.99,508.118,356.99,505.941L356.99,505.941zM878.342,505.941c0-2.177-4.661-6.82-11.309-6.82s-11.308,4.643-11.308,6.82s4.661,6.82,11.308,6.82S878.342,508.118,878.342,505.941L878.342,505.941zM586.767,414.969c7.664,0,13.854-6.198,13.854-13.854c0-7.664-6.19-13.854-13.854-13.854c-7.654,0-13.854,6.19-13.854,13.854C572.913,408.771,579.113,414.969,586.767,414.969L586.767,414.969zM640-86h-640v640h640V-86L640-86zM568.629,413.782c-7.664,0-13.854,6.198-13.854,13.854c0,7.664,6.19,13.854,13.854,13.854c7.654,0,13.854-6.19,13.854-13.854C582.483,419.98,576.293,413.782,568.629,413.782L568.629,413.782zM630,86h-620v620h620V86L630,86z',
                            onclick: (function(id) { return function() { toggleFullscreen(id); }; })(chartId)
                        },
                        restore: {},
                        saveAsImage: {}
                    },
                    right: 40
                },
                dataZoom: [{ type: 'inside', zoomOnMouseWheel: false, start: 0, end: 100, xAxisIndex: 0, yAxisIndex: 0 }, { type: 'slider', start: 0, end: 100, xAxisIndex: 0, yAxisIndex: 0 }],
                dataset: { source: dataRows },
                series: series
            };

            initChart(chartId, option);
        }

        function createTimeSeriesChart(chartId, labels, dataRows, title, yLabel) {
            const series = labels.map((label, index) => ({
                name: label,
                type: 'line',
                smooth: true,
                showSymbol: false,
                encode: { x: 0, y: index + 1 }
            }));

            const option = {
                title: { text: title },
                tooltip: { trigger: 'axis' },
                xAxis: { name: 'Iteration', type: 'value' },
                yAxis: { name: yLabel, type: 'value' },
                toolbox: {
                    show: true,
                    feature: {
                        myFullscreen: {
                            show: true,
                            title: 'Fullscreen',
                            icon: 'path://M432.45,595.444c0,2.177-4.661,6.82-11.308,6.82c-6.648,0-11.309-4.643-11.309-6.82s4.661-6.82,11.309-6.82C427.789,588.624,432.45,593.267,432.45,595.444L432.45,595.444zM586.767,666.258c7.656,0,13.854,6.337,13.854,14.141c0,7.804-6.198,14.141-13.854,14.141c-7.664,0-13.854-6.337-13.854-14.141C572.913,672.595,579.103,666.258,586.767,666.258L586.767,666.258zM568.629,667.071c-7.654,0-13.854,6.198-13.854,13.854c0,7.664,6.2,13.854,13.854,13.854c7.664,0,13.854-6.19,13.854-13.854C582.483,673.269,576.293,667.071,568.629,667.071L568.629,667.071zM356.99,505.941c0-2.177,4.661-6.82,11.309-6.82c6.648,0,11.308,4.643,11.308,6.82s-4.66,6.82-11.308,6.82C361.651,512.761,356.99,508.118,356.99,505.941L356.99,505.941zM878.342,505.941c0-2.177-4.661-6.82-11.309-6.82s-11.308,4.643-11.308,6.82s4.661,6.82,11.308,6.82S878.342,508.118,878.342,505.941L878.342,505.941zM586.767,414.969c7.664,0,13.854-6.198,13.854-13.854c0-7.664-6.19-13.854-13.854-13.854c-7.654,0-13.854,6.19-13.854,13.854C572.913,408.771,579.113,414.969,586.767,414.969L586.767,414.969zM640-86h-640v640h640V-86L640-86zM568.629,413.782c-7.664,0-13.854,6.198-13.854,13.854c0,7.664,6.19,13.854,13.854,13.854c7.654,0,13.854-6.19,13.854-13.854C582.483,419.98,576.293,413.782,568.629,413.782L568.629,413.782zM630,86h-620v620h620V86L630,86z',
                            onclick: (function(id) { return function() { toggleFullscreen(id); }; })(chartId)
                        },
                        restore: {},
                        saveAsImage: {}
                    },
                    right: 40
                },
                dataZoom: [{ type: 'inside', zoomOnMouseWheel: false, xAxisIndex: 0, yAxisIndex: 0 }, { type: 'slider', xAxisIndex: 0, yAxisIndex: 0 }],
                dataset: { source: dataRows },
                series: series
            };

            initChart(chartId, option);
        }

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

        function toggleSidebar() {
            const sidebar = document.getElementById('mySidebar');
            const content = document.querySelector('.main-content');
            sidebar.classList.toggle('w3-hide-small');
            sidebar.classList.toggle('w3-hide-medium');
            content.classList.toggle('sidebar-hidden');
            setTimeout(() => {
                charts.forEach(chart => chart.resize());
            }, 310);
        }

        function tableToArray(tableId) {
            const table = document.getElementById(tableId);
            if (!table) {
                console.error('Table not found: ' + tableId);
                return [];
            }
            
            const headers = [];
            const data = [];
            
            // Extract headers from thead
            const thead = table.querySelector('thead');
            if (thead) {
                const headerRow = thead.querySelector('tr');
                if (headerRow) {
                    headerRow.querySelectorAll('th').forEach(th => {
                        headers.push(th.textContent.trim());
                    });
                }
            }
            
            // Extract data from tbody
            const tbody = table.querySelector('tbody');
            if (tbody) {
                tbody.querySelectorAll('tr').forEach(row => {
                    const rowData = {};
                    const cells = row.querySelectorAll('td');
                    cells.forEach((cell, index) => {
                        const header = headers[index] || 'Column ' + (index + 1);
                        rowData[header] = cell.textContent.trim();
                    });
                    data.push(rowData);
                });
            }
            
            return data;
        }

        function arrayToCsv(data, delimiter = ',') {
            if (!data || data.length === 0) {
                return '';
            }
            
            const headers = Object.keys(data[0]);
            const csvHeaders = headers.map(h => {
                // Escape quotes and wrap in quotes if contains delimiter or quotes or newlines
                if (h.includes(delimiter) || h.includes('"') || h.includes('\n')) {
                    return '"' + h.replace(/"/g, '""') + '"';
                }
                return h;
            }).join(delimiter);
            
            const csvRows = data.map(row => {
                return headers.map(header => {
                    const value = row[header] || '';
                    // Escape quotes and wrap in quotes if contains delimiter or quotes or newlines
                    if (value.includes(delimiter) || value.includes('"') || value.includes('\n')) {
                        return '"' + value.replace(/"/g, '""') + '"';
                    }
                    return value;
                }).join(delimiter);
            });
            
            return [csvHeaders, ...csvRows].join('\n');
        }

        function downloadFile(content, fileName, mimeType) {
            const blob = new Blob([content], { type: mimeType });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
        }

        function downloadTableAsCSV(tableId, fileName) {
            const data = tableToArray(tableId);
            const csv = arrayToCsv(data);
            downloadFile(csv, fileName, 'text/csv;charset=utf-8;');
        }

        function downloadTableAsJSON(tableId, fileName) {
            const data = tableToArray(tableId);
            const json = JSON.stringify(data, null, 2);
            downloadFile(json, fileName, 'application/json;charset=utf-8;');
        }

        window.addEventListener('resize', () => {
            charts.forEach(chart => chart.resize());
        });

        window.addEventListener('hashchange', updateActiveLink);

        window.addEventListener('DOMContentLoaded', () => {
            updateEchartsTheme();
            updateActiveLink();

            const targetSections = document.querySelectorAll('div[id], article[id], section[id]');
            const navLinks = document.querySelectorAll('.nav-link');

            const observerOptions = {
                root: null,
                rootMargin: '-10% 0px -70% 0px',
                threshold: 0
            };

            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const id = entry.target.getAttribute('id');
                        if (id) {
                            navLinks.forEach(link => {
                                const href = link.getAttribute('href');
                                if (href === '#' + id) {
                                    navLinks.forEach(l => l.classList.remove('active'));
                                    link.classList.add('active');
                                }
                            });
                        }
                    }
                });
            }, observerOptions);

            targetSections.forEach((section) => {
                observer.observe(section);
            });
        });
        """.trimIndent()
    }
}
