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
            const dummy = document.createElement('div');
            dummy.style.visibility = 'hidden';
            dummy.style.position = 'absolute';
            document.body.appendChild(dummy);

            const getColor = (classes) => {
                dummy.className = classes;
                return getComputedStyle(dummy).backgroundColor;
            };
            const getTextColor = (classes) => {
                dummy.className = classes;
                return getComputedStyle(dummy).color;
            };

            // Diversified palette using standard W3.CSS framework colors
            const palette = [
                getColor('w3-theme'), // Use primary theme color first
                getColor('w3-blue'),
                getColor('w3-red'),
                getColor('w3-green'),
                getColor('w3-orange'),
                getColor('w3-purple'),
                getColor('w3-teal'),
                getColor('w3-indigo'),
                getColor('w3-amber'),
                getColor('w3-pink')
            ];

            const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
            const cardClass = isDark ? 'w3-theme-d4' : 'w3-theme-l5';
            const bg = getColor(cardClass);
            const text = getTextColor(cardClass);
            const muted = getTextColor('w3-text-theme');
            const accent = getColor('w3-theme');
            const border = isDark ? '#444' : '#ddd';

            document.body.removeChild(dummy);

            return { palette, bg, text, muted, border, accent };
        }

        function updateEchartsTheme() {
            const colors = probeColors();
            
            echartsTheme = {
                backgroundColor: colors.bg,
                textStyle: { color: colors.text, fontFamily: 'system-ui, sans-serif' },
                title: { textStyle: { color: colors.text, fontSize: 14, fontWeight: 'bold' } },
                legend: { 
                    textStyle: { color: colors.text, fontSize: 11 },
                    pageTextStyle: { color: colors.text },
                    backgroundColor: colors.bg,
                    borderColor: colors.border,
                    borderWidth: 1,
                    borderRadius: 4,
                    padding: 5
                },
                tooltip: { 
                    backgroundColor: colors.bg,
                    borderColor: colors.accent,
                    textStyle: { color: colors.text, fontSize: 12 },
                    confine: true
                },
                grid: { left: '15%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
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
                // ECharts merges setOption by default, so we just need to send the theme properties
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
                const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                next = isDark ? 'light' : 'dark';
            }
            
            document.documentElement.setAttribute('data-theme', next);
            localStorage.setItem('tulip-theme', next);
            location.reload(); 
        }

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
            myChart.setOption(mergedOption);
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
                tooltip: { trigger: 'axis' },
                legend: { data: labels, orient: 'vertical', left: 'left', top: 'middle', type: 'scroll' },
                grid: { left: '15%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
                toolbox: {
                    show: true,
                    feature: {
                        dataZoom: { yAxisIndex: 'none' },
                        restore: {},
                        saveAsImage: {}
                    },
                    right: 40
                },
                dataZoom: [{ type: 'inside', start: 0, end: 100 }, { start: 0, end: 100 }],
                xAxis: { name: 'Percentile', type: 'value', min: 50, max: 100 },
                yAxis: { name: 'Latency (' + unit + ')', type: 'value' },
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
                legend: { data: labels, orient: 'vertical', left: 'left', top: 'middle', type: 'scroll' },
                grid: { left: '15%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
                toolbox: {
                    show: true,
                    feature: {
                        dataZoom: { yAxisIndex: 'none' },
                        restore: {},
                        saveAsImage: {}
                    },
                    right: 40
                },
                dataZoom: [{ type: 'inside' }, { type: 'slider' }],
                xAxis: { name: 'Iteration', type: 'value' },
                yAxis: { name: yLabel, type: 'value' },
                dataset: { source: dataRows },
                series: series
            };

            initChart(chartId, option);
        }

        function toggleFullscreen(chartId) {
            const card = document.getElementById(chartId).closest('.card');
            card.classList.toggle('fullscreen');
            const chart = charts.get(chartId);
            if (chart) {
                setTimeout(() => chart.resize(), 310);
            }
            const btn = card.querySelector('.btn-fullscreen');
            if (card.classList.contains('fullscreen')) {
                btn.innerHTML = '縮';
                document.body.style.overflow = 'hidden';
            } else {
                btn.innerHTML = '盍';
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

        window.addEventListener('resize', () => {
            charts.forEach(chart => chart.resize());
        });

        window.addEventListener('DOMContentLoaded', () => {
            updateEchartsTheme();

            const sections = document.querySelectorAll('#overview, #config, #runtime, h3');
            const navLinks = document.querySelectorAll('.nav-link');

            const observerOptions = {
                root: null,
                rootMargin: '-10% 0px -80% 0px',
                threshold: 0
            };

            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const id = entry.target.getAttribute('id');
                        if (id) {
                            navLinks.forEach(link => {
                                link.classList.remove('active', 'w3-theme-l3');
                                if (link.getAttribute('href') === '#' + id) {
                                    link.classList.add('active', 'w3-theme-l3');
                                }
                            });
                        }
                    }
                });
            }, observerOptions);

            sections.forEach((section) => {
                observer.observe(section);
            });

            navLinks.forEach(link => {
                link.addEventListener('click', (e) => {
                    navLinks.forEach(l => l.classList.remove('active', 'w3-theme-l3'));
                    link.classList.add('active', 'w3-theme-l3');
                });
            });
        });
        """.trimIndent()
    }
}
