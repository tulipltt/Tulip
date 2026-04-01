package io.github.tulipltt.tulip.report

object ReportScripts {
    val echartsBase = """
        const chartColors = ['#C735F7', '#FF0087', '#F8D82E', '#73bf69', '#3274d9', '#ff9830', '#9456ff', '#e67e22', '#2ecc71', '#e74c3c'];
        
        const echartsTheme = {
            backgroundColor: '#181b1f',
            textStyle: { color: '#d8d9da', fontFamily: 'Inter, system-ui, sans-serif' },
            title: { textStyle: { color: '#d8d9da', fontSize: 14, fontWeight: 'bold' } },
            legend: { textStyle: { color: '#8e8e8e', fontSize: 11 } },
            tooltip: { 
                backgroundColor: 'rgba(24, 27, 31, 0.95)',
                borderColor: '#2c3235',
                textStyle: { color: '#d8d9da', fontSize: 12 }
            },
            grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true, borderColor: '#2c3235' },
            categoryAxis: {
                axisLine: { lineStyle: { color: '#2c3235' } },
                axisLabel: { color: '#8e8e8e', fontSize: 10 },
                splitLine: { show: false }
            },
            valueAxis: {
                axisLine: { lineStyle: { color: '#2c3235' } },
                axisLabel: { color: '#8e8e8e', fontSize: 10 },
                splitLine: { lineStyle: { color: '#2c3235', type: 'dashed' } }
            }
        };

        const charts = new Map();

        function initChart(chartId, option) {
            const chartDom = document.getElementById(chartId);
            const myChart = echarts.init(chartDom);
            myChart.setOption({ ...echartsTheme, ...option });
            charts.set(chartId, myChart);
            return myChart;
        }

        function createPercentileChart(chartId, labels, dataRows, title, unit) {
            const series = labels.map((label, index) => ({
                name: label,
                type: 'line',
                smooth: true,
                showSymbol: false,
                color: chartColors[index % chartColors.length],
                encode: { x: 0, y: index + 1 }
            }));

            const option = {
                title: { text: title },
                tooltip: { trigger: 'axis' },
                legend: { data: labels, bottom: 0, type: 'scroll' },
                toolbox: {
                    show: true,
                    feature: {
                        dataZoom: { yAxisIndex: 'none' },
                        restore: {},
                        saveAsImage: {}
                    },
                    iconStyle: { borderColor: '#8e8e8e' },
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
                color: chartColors[index % chartColors.length],
                encode: { x: 0, y: index + 1 }
            }));

            const option = {
                title: { text: title },
                tooltip: { trigger: 'axis' },
                legend: { data: labels, bottom: 0, type: 'scroll' },
                toolbox: {
                    show: true,
                    feature: {
                        dataZoom: { yAxisIndex: 'none' },
                        restore: {},
                        saveAsImage: {}
                    },
                    iconStyle: { borderColor: '#8e8e8e' },
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
            const card = document.getElementById(chartId).parentElement;
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
            const sidebar = document.querySelector('.sidebar');
            const content = document.querySelector('.main-content');
            sidebar.classList.toggle('hidden');
            content.classList.toggle('sidebar-hidden');
            setTimeout(() => {
                charts.forEach(chart => chart.resize());
            }, 310);
        }

        window.addEventListener('resize', () => {
            charts.forEach(chart => chart.resize());
        });

        // Scroll Spy Logic
        window.addEventListener('DOMContentLoaded', () => {
            const sections = document.querySelectorAll('#overview, #config, #runtime, .section-title');
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
                        navLinks.forEach(link => {
                            link.classList.remove('active');
                            if (link.getAttribute('href') === '#' + id) {
                                link.classList.add('active');
                            }
                        });
                    }
                });
            }, observerOptions);

            sections.forEach((section) => {
                observer.observe(section);
            });

            // Handle manual link clicks for sidebar active state
            navLinks.forEach(link => {
                link.addEventListener('click', (e) => {
                    navLinks.forEach(l => l.classList.remove('active'));
                    link.classList.add('active');
                });
            });
        });
    """.trimIndent()
}
