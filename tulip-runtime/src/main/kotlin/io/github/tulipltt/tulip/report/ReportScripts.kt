package io.github.tulipltt.tulip.report

object ReportScripts {
    val googleChartsBase = """
        google.charts.load('current', {'packages':['corechart', 'line']});
        
        const chartOptionsBase = {
            backgroundColor: '#181b1f',
            chartArea: { backgroundColor: '#181b1f', left: '10%', right: '5%', top: '10%', bottom: '20%' },
            legend: { 
                position: 'bottom', 
                textStyle: { color: '#d8d9da', fontSize: 10 },
                maxLines: 5
            },
            focusTarget: 'category',
            tooltip: { 
                trigger: 'both', 
                textStyle: { fontSize: 11 },
                showColorCode: true
            },
            explorer: {
                actions: ['dragToZoom', 'rightClickToReset'],
                axis: 'horizontal',
                keepInBounds: true,
                maxZoomIn: 8.0
            },
            hAxis: { 
                textStyle: { color: '#8e8e8e', fontSize: 10 }, 
                titleTextStyle: { color: '#d8d9da', fontSize: 11 }, 
                gridlines: { color: '#2c3235' },
                minorGridlines: { color: '#1c2022' }
            },
            vAxis: { 
                textStyle: { color: '#8e8e8e', fontSize: 10 }, 
                titleTextStyle: { color: '#d8d9da', fontSize: 11 }, 
                gridlines: { color: '#2c3235' },
                minorGridlines: { color: '#1c2022' }
            },
            titleTextStyle: { color: '#d8d9da', fontSize: 14, bold: true },
            colors: ['#C735F7', '#FF0087', '#F8D82E', '#73bf69', '#3274d9', '#ff9830', '#9456ff', '#e67e22', '#2ecc71', '#e74c3c']
        };

        // Track hidden series per chart
        const hiddenSeries = {};

        function addSeriesToggle(chart, data, options, chartId) {
            if (!hiddenSeries[chartId]) hiddenSeries[chartId] = new Set();
            
            google.visualization.events.addListener(chart, 'select', function() {
                var selection = chart.getSelection();
                if (selection.length > 0) {
                    if (selection[0].row === null) { // Legend click
                        var col = selection[0].column;
                        if (hiddenSeries[chartId].has(col)) {
                            hiddenSeries[chartId].delete(col);
                        } else {
                            hiddenSeries[chartId].add(col);
                        }
                        
                        var view = new google.visualization.DataView(data);
                        var columns = [0];
                        var series = {};
                        
                        for (var i = 1; i < data.getNumberOfColumns(); i++) {
                            if (hiddenSeries[chartId].has(i)) {
                                columns.push({
                                    label: data.getColumnLabel(i),
                                    type: data.getColumnType(i),
                                    calc: function() { return null; }
                                });
                                series[i-1] = { color: '#444' };
                            } else {
                                columns.push(i);
                                var colorIdx = (i - 1) % chartOptionsBase.colors.length;
                                series[i-1] = { color: chartOptionsBase.colors[colorIdx] };
                            }
                        }
                        
                        var updatedOptions = {
                            ...options,
                            series: series
                        };
                        
                        view.setColumns(columns);
                        chart.draw(view, updatedOptions);
                    }
                }
            });
        }

        function drawPercentileChart(chartId, dataRows, title, unit) {
            var data = new google.visualization.DataTable();
            data.addColumn('number', 'Percentile');
            data.addColumn('number', 'Latency');
            data.addRows(dataRows);

            var options = {
                ...chartOptionsBase,
                title: title,
                hAxis: {
                    ...chartOptionsBase.hAxis,
                    title: 'Percentile',
                    ticks: [
                        {v:0, f:'0%'}, {v:10, f:'10%'}, {v:20, f:'20%'}, {v:30, f:'30%'}, {v:40, f:'40%'},
                        {v:50, f:'50%'}, {v:60, f:'60%'}, {v:70, f:'70%'}, {v:80, f:'80%'}, {v:90, f:'90%'}, 
                        {v:100, f:'100%'}
                    ]
                },
                vAxis: { ...chartOptionsBase.vAxis, title: 'Latency (' + unit + ')', minValue: 0 },
                curveType: 'function'
            };

            var chart = new google.visualization.LineChart(document.getElementById(chartId));
            chart.draw(data, options);
            addSeriesToggle(chart, data, options, chartId);
            window['chart_obj_' + chartId] = chart;
        }

        function drawCombinedPercentileChart(chartId, labels, dataRows, title, unit) {
            var data = new google.visualization.DataTable();
            data.addColumn('number', 'Percentile');
            labels.forEach(label => { data.addColumn('number', label); });
            data.addRows(dataRows);

            var options = {
                ...chartOptionsBase,
                title: title,
                hAxis: {
                    ...chartOptionsBase.hAxis,
                    title: 'Percentile',
                    ticks: [
                        {v:0, f:'0%'}, {v:10, f:'10%'}, {v:20, f:'20%'}, {v:30, f:'30%'}, {v:40, f:'40%'},
                        {v:50, f:'50%'}, {v:60, f:'60%'}, {v:70, f:'70%'}, {v:80, f:'80%'}, {v:90, f:'90%'}, 
                        {v:100, f:'100%'}
                    ]
                },
                vAxis: { ...chartOptionsBase.vAxis, title: 'Latency (' + unit + ')', minValue: 0 },
                curveType: 'function'
            };

            var chart = new google.visualization.LineChart(document.getElementById(chartId));
            chart.draw(data, options);
            addSeriesToggle(chart, data, options, chartId);
            window['chart_obj_' + chartId] = chart;
        }

        function drawCombinedTimeSeriesChart(chartId, labels, dataRows, title, yLabel) {
            var data = new google.visualization.DataTable();
            data.addColumn('number', 'Iteration');
            labels.forEach(label => { data.addColumn('number', label); });
            data.addRows(dataRows);

            var options = {
                ...chartOptionsBase,
                title: title,
                hAxis: { ...chartOptionsBase.hAxis, title: 'Iteration' },
                vAxis: { ...chartOptionsBase.vAxis, title: yLabel, minValue: 0 }
            };

            var chart = new google.visualization.LineChart(document.getElementById(chartId));
            chart.draw(data, options);
            addSeriesToggle(chart, data, options, chartId);
            window['chart_obj_' + chartId] = chart;
        }

        function downloadChart(chartId, fileName) {
            var chart = window['chart_obj_' + chartId];
            if (chart) {
                var imgUri = chart.getImageURI();
                var link = document.createElement('a');
                link.href = imgUri;
                link.download = fileName + '.png';
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            }
        }
    """.trimIndent()
}
