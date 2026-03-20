package io.github.tulipltt.tulip.report

object ReportScripts {
    val googleChartsBase = """
        google.charts.load('current', {'packages':['corechart', 'line']});
        
        function drawPercentileChart(chartId, dataPoints, title, unit) {
            var data = new google.visualization.DataTable();
            data.addColumn('number', 'Percentile');
            data.addColumn('number', 'Latency');
            
            data.addRows(dataPoints);

            var options = {
                title: title,
                hAxis: {
                    title: 'Percentile',
                    logScale: true,
                    ticks: [
                        {v:1, f:'0%'}, 
                        {v:10, f:'90%'}, 
                        {v:100, f:'99%'}, 
                        {v:1000, f:'99.9%'}, 
                        {v:10000, f:'99.99%'}, 
                        {v:100000, f:'99.999%'}
                    ]
                },
                vAxis: {
                    title: 'Latency (' + unit + ')'
                },
                backgroundColor: '#181b1f',
                chartArea: { backgroundColor: '#181b1f' },
                legend: { position: 'bottom', textStyle: { color: '#d8d9da' } },
                hAxis: { textStyle: { color: '#d8d9da' }, titleTextStyle: { color: '#d8d9da' }, gridlines: { color: '#2c3235' } },
                vAxis: { textStyle: { color: '#d8d9da' }, titleTextStyle: { color: '#d8d9da' }, gridlines: { color: '#2c3235' } },
                titleTextStyle: { color: '#d8d9da' }
            };

            var chart = new google.visualization.LineChart(document.getElementById(chartId));
            chart.draw(data, options);
        }

        function drawTimeSeriesChart(chartId, dataPoints, title, yLabel) {
            var data = new google.visualization.DataTable();
            data.addColumn('number', 'Iteration');
            data.addColumn('number', yLabel);
            
            data.addRows(dataPoints);

            var options = {
                title: title,
                hAxis: { title: 'Iteration', textStyle: { color: '#d8d9da' }, titleTextStyle: { color: '#d8d9da' } },
                vAxis: { title: yLabel, textStyle: { color: '#d8d9da' }, titleTextStyle: { color: '#d8d9da' } },
                backgroundColor: '#181b1f',
                legend: { position: 'none' },
                gridlines: { color: '#2c3235' },
                titleTextStyle: { color: '#d8d9da' }
            };

            var chart = new google.visualization.LineChart(document.getElementById(chartId));
            chart.draw(data, options);
        }
    """.trimIndent()
}
