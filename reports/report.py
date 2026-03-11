from __future__ import print_function
import datetime
import json
import re
import sys
import org.HdrHistogram.Histogram as Histogram
import os
from collections import OrderedDict
import java.io.PrintStream as PrintStream
import java.io.ByteArrayOutputStream as ByteArrayOutputStream
try:
    import io.github.tulipltt.tulip.stats.LlqHistogram as LlqHistogram
except:
    print("LlqHistogram class not found, LLQ stats will be missing.")
import java.lang.management.ManagementFactory as ManagementFactory
import java.util.stream.Collectors as Collectors
import java.lang.System as System
import org.asciidoctor.Asciidoctor as Asciidoctor
import org.asciidoctor.Attributes as Attributes
import org.asciidoctor.Options as Options
import org.asciidoctor.SafeMode as SafeMode
import java.io.File as File
# /// jbang
# requires-jython = "2.7.4"
# requires-java = "21"
# runtime-options = [
#   "-XX:+IgnoreUnrecognizedVMOptions",
#   "--enable-native-access=ALL-UNNAMED",
#   "--sun-misc-unsafe-memory-access=allow"
# ],
# dependencies = [
#   "com.google.code.gson:gson:2.13.1",
#   "org.hdrhistogram:HdrHistogram:2.2.2",
#   "org.asciidoctor:asciidoctorj:3.0.1",
#   "org.asciidoctor:asciidoctorj-diagram:3.1.0",
#   "org.asciidoctor:asciidoctorj-diagram-plantuml:1.2025.3"
# ]
# ///

summary_html_1 = '''<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style type="text/css">
    div.histo {
         display: none
    }
</style>

<!--Load the AJAX API-->
<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script type="text/javascript">

    if (window.File && window.FileReader && window.FileList && window.Blob) {
        // Great success! All the File APIs are supported.
    } else {
        alert('The File APIs are not fully supported in this browser.');
    }

    // Load the Visualization API and the corechart package.
    google.load('visualization', '1.0', {'packages':['corechart']});

    // Set a callback to run when the Google Visualization API is loaded.
    google.setOnLoadCallback(drawInitialChart);

    var chartData = null;
    var chart = null;

    function setChartData(names, histos) {
        while (names.length < histos.length) {
            names.push('Unknown');
        }

        var series = [];
        for (var i = 0; i < histos.length; i++) {
            series = appendDataSeries(histos[i], names[i], series);
        }

        chartData = google.visualization.arrayToDataTable(series);
    }

'''

text_block_1 = '''
    function drawInitialChart() {
        // Connect the choose files button:
        document.getElementById('files').addEventListener('change', handleFileSelect, false);

        // Load some static example data:
        var data1Str = document.querySelector("div#data_1").innerHTML.trim();
        var data2Str = document.querySelector("div#data_2").innerHTML.trim();
        var data3Str = document.querySelector("div#data_3").innerHTML.trim();
        var histos = [data3Str, data2Str, data1Str];
        var names = ['A', 'B', 'C'];

        setChartData(names, histos);
        drawChart();
    }
'''

summary_html_2 = '''
    var maxPercentile = 1000000;

    function drawChart() {

        var ticks =
                [{v:1,f:'0%'},
                    {v:10,f:'90%'},
                    {v:100,f:'99%'},
                    {v:1000,f:'99.9%'},
                    {v:10000,f:'99.99%'},
                    {v:100000,f:'99.999%'},
                    {v:1000000,f:'99.9999%'},
                    {v:10000000,f:'99.99999%'},
                    {v:100000000,f:'99.999999%'}];

        var unitSelection = document.getElementById("timeUnitSelection");
        var unitSelIndex = unitSelection.selectedIndex;
        var unitText = unitSelection.options[unitSelIndex].innerHTML;

        var options = {
//            title: 'Percentile Response Time Distribution',
            height: 480,
//            hAxis: {title: 'Percentile', minValue: 0, logScale: true, ticks:ticks },
            hAxis: {
                title: "Percentile",
                minValue: 1, logScale: true, ticks:ticks,
                viewWindowMode:'explicit',
                viewWindow:{
                    max:maxPercentile,
                    min:1
                }
            },
            vAxis: {title: 'Latency (' + unitText + ')', minValue: 0 },
            legend: {position: 'bottom'}
        };


        // add tooltips with correct percentile text to data:
        var columns = [0];
        for (var i = 1; i < chartData.getNumberOfColumns(); i++) {
            columns.push(i);
            columns.push({
                type: 'string',
                properties: {
                    role: 'tooltip'
                },
                calc: (function (j) {
                    return function (dt, row) {
                        var percentile = 100.0 - (100.0/dt.getValue(row, 0));
                        return dt.getColumnLabel(j) + ': ' +
                                percentile.toPrecision(7) +
                                '\%\\'ile = ' + dt.getValue(row, j) + ' ' + unitText
                    }
                })(i)
            });
        }
        var view = new google.visualization.DataView(chartData);
        view.setColumns(columns);

        chart = new google.visualization.LineChart(document.getElementById('chart_div'));
        chart.draw(view, options);

        google.visualization.events.addListener(chart, 'ready', function () {
            chart_div.innerHTML = '<img src="' + chart.getImageURI() + '">';
        });

    }
</script>
<script type="text/javascript">
    function appendDataSeries(histo, name, dataSeries) {
        var series;
        var seriesCount;
        if (dataSeries.length == 0) {
            series = [ ['X', name] ];
            seriesCount = 1;
        } else {
            series = dataSeries;
            series[0].push(name);
            seriesCount = series[0].length - 1;
        }

        var lines = histo.split("\\n");

        var seriesIndex = 1;
        for (var i = 0; i < lines.length; i++) {
            var line = lines[i].trim();
            var values = line.trim().split(/[ ]+/);

            if (line[0] != '#' && values.length == 4) {

                var y = parseFloat(values[0]);
                var x = parseFloat(values[3]);

                if (!isNaN(x) && !isNaN(y)) {

                    if (seriesIndex >= series.length) {
                        series.push([x]);
                    }

                    while (series[seriesIndex].length < seriesCount) {
                        series[seriesIndex].push(null);
                    }

                    series[seriesIndex].push(y);
                    seriesIndex++;
                }
            }
        }

        while (seriesIndex < series.length) {
            series[seriesIndex].push(null);
            seriesIndex++;
        }

        return series;
    }
</script>
<script>
    function timeUnitsSelected(evt) {
        drawChart();
        return {typed: ''};
    }

    function doExport(event) {
        saveSvgAsPng(document.querySelector('svg'), 'Histogram', 2.0);
        return {typed: ''};
    }
</script>

<script>
    function handleFileSelect(evt) {
        var files = evt.target.files; // FileList object
        var fileDisplayArea = document.getElementById('fileDisplayArea');

        var names = [];
        var histos = [];

        fileDisplayArea.innerText = "file selected...\\n";

        // Loop through the FileList and render image files as thumbnails.
        for (var i = 0, f; f = files[i]; i++) {
            var reader = new FileReader();

            reader.onload = (function(theFile) {
                return function(e) {
                    histos.push(e.target.result);
                    names.push(escape(theFile.name));
                    fileDisplayArea.innerText = " Plotting input from: " + names + "\\n";
                    setChartData(names, histos);
                    drawChart();
                };
            })(f);

            // Read in the image file as a data URL.
            reader.readAsText(f);
        }

    }

</script>

<script type="text/javascript">
    (function() {
        var out$ = typeof exports != 'undefined' && exports || this;

        var doctype = '<?xml version="1.0" standalone="no"?><!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">';

        function inlineImages(callback) {
            var images = document.querySelectorAll('svg image');
            var left = images.length;
            if (left == 0) {
                callback();
            }
            for (var i = 0; i < images.length; i++) {
                (function(image) {
                    if (image.getAttribute('xlink:href')) {
                        var href = image.getAttribute('xlink:href').value;
                        if (/^http/.test(href) && !(new RegExp('^' + window.location.host).test(href))) {
                            throw new Error("Cannot render embedded images linking to external hosts.");
                        }
                    }
                    var canvas = document.createElement('canvas');
                    var ctx = canvas.getContext('2d');
                    var img = new Image();
                    img.src = image.getAttribute('xlink:href');
                    img.onload = function() {
                        canvas.width = img.width;
                        canvas.height = img.height;
                        ctx.drawImage(img, 0, 0);
                        image.setAttribute('xlink:href', canvas.toDataURL('image/png'));
                        left--;
                        if (left == 0) {
                            callback();
                        }
                    }
                })(images[i]);
            }
        }

        function styles(dom) {
            var css = "";
            var sheets = document.styleSheets;
            for (var i = 0; i < sheets.length; i++) {
                if (sheets[i].hasOwnProperty('cssRules')) {
                    var rules = sheets[i].cssRules;
                    for (var j = 0; j < rules.length; j++) {
                        var rule = rules[j];
                        if (typeof(rule.style) != "undefined") {
                            css += rule.selectorText + " { " + rule.style.cssText + " }\\n";
                        }
                    }
                }
            }

            var s = document.createElement('style');
            s.setAttribute('type', 'text/css');
            s.innerHTML = "<![CDATA[\\n" + css + "\\n]]>";

            var defs = document.createElement('defs');
            defs.appendChild(s);
            return defs;
        }

        out$.svgAsDataUri = function(el, scaleFactor, cb) {
            scaleFactor = scaleFactor || 1;

            inlineImages(function() {
                var outer = document.createElement("div");
                var clone = el.cloneNode(true);
                var width = parseInt(
                        clone.getAttribute('width')
                        || clone.style.width
                        || out$.getComputedStyle(el).getPropertyValue('width')
                );
                var height = parseInt(
                        clone.getAttribute('height')
                        || clone.style.height
                        || out$.getComputedStyle(el).getPropertyValue('height')
                );

                var xmlns = "http://www.w3.org/2000/xmlns/";

                clone.setAttribute("version", "1.1");
                clone.setAttributeNS(xmlns, "xmlns", "http://www.w3.org/2000/svg");
                clone.setAttributeNS(xmlns, "xmlns:xlink", "http://www.w3.org/1999/xlink");
                clone.setAttribute("width", width * scaleFactor);
                clone.setAttribute("height", height * scaleFactor);
                clone.setAttribute("viewBox", "0 0 " + width + " " + height);
                outer.appendChild(clone);

                clone.insertBefore(styles(clone), clone.firstChild);

                var svg = doctype + outer.innerHTML;
                var uri = 'data:image/svg+xml;base64,' + window.btoa(unescape(encodeURIComponent(svg)));
                if (cb) {
                    cb(uri);
                }
            });
        }

        out$.saveSvgAsPng = function(el, name, scaleFactor) {
            out$.svgAsDataUri(el, scaleFactor, function(uri) {
                var image = new Image();
                image.src = uri;
                image.onload = function() {
                    var canvas = document.createElement('canvas');
                    canvas.width = image.width;
                    canvas.height = image.height;
                    var context = canvas.getContext('2d');
                    context.drawImage(image, 0, 0);

                    var a = document.createElement('a');
                    a.download = name;
                    a.href = canvas.toDataURL('image/png');
                    document.body.appendChild(a);
                    a.click();
                }
            });
        }
    })();
</script>

<style>
    .slider-width500
    {
        width: 500px;
    }
</style>

</head>

<body>

__CHARTS_TEXT__

<h2>Percentile Response Time Distribution</h2>

<input type="file" id="files" name="files[]" multiple />

<pre id="fileDisplayArea">Please select file(s) above.</pre>

<!--Div that will hold the chart-->
<div id="chart_div">None Loaded</div>

Latency time units:
<select name="units" size="1" id="timeUnitSelection" onChange="timeUnitsSelected()">
    <option value="Latency (seconds)">seconds</option>
    <option selected value="Latency (milliseconds)">milliseconds</option>
    <option value="Latency (qs)">microseconds</option>
    <option value="Latency (nanoseconds)">nanoseconds</option>
</select>
<button type='button' onclick='doExport(event)'>Export Image</button>

&nbsp; &nbsp; &nbsp; &nbsp;
<p>
Percentile range:

<input type="range" class="slider-width500"
       min="1" max="8" value="7" step="1"
       width="300px"
       onchange="showValue(this.value)" />
<span id="percentileRange">99.99999%</span>
<script type="text/javascript">
    function showValue(newValue) {
        var x = Math.pow(10, newValue);
        var percentile = 100.0 - (100.0 / x);
        document.getElementById("percentileRange").innerHTML=percentile + "%";
        maxPercentile = x;
        drawChart();
        return {typed: ''};
    }
</script>
</p>
'''

summary_html_table_1 = '''
<style>
table, th, td {
  border:1px solid black; font-size:16px; text-align: center;
}
mark {
  background-color: LightGray;
  color: black;
}
</style>

<h2>Percentile Response Time Distribution (HDR)</h2>
'''

summary_html_table_2 = '''
<h2>Percentile Response Time Distribution (LLQ)</h2>

'''

summary_html_3 = '''</body>
</html>
'''

# <h2><a href="https://tulipltt.github.io/Tulip-docs">__DESC1__</a> / __DESC2__</h2>
# <h2>__DESC1__ / __DESC2__</h2>

header = '''<!DOCTYPE html>
<html lang="en">

<style>
table, th, td {
  border:1px solid black; font-size:16px; text-align: center;
}

/* Logic: Hide the last 8 columns by default */
.extra-col {
    display: none;
    background-color: #E5E4E2; /* Subtle tint for expanded columns */
}

/* Logic: Show them when the 'expanded' class is present */
table.expanded .extra-col {
    display: table-cell;
}
</style>

<body>

<h2><a href="https://tulipltt.github.io/Tulip-docs">__DESC1__</a> / __DESC2__</h2>

<button id="toggleBtn">Show All Columns</button>

<table style="width:100%" id="dataTable">
  <tr>
    <th>Benchmark</th>
    <th>Run Id</th>
    <th>#N</th>
    <th>#F</th>
    <th>Time</th>
    <th>APS</th>
    <th>Avg_Rt</th>
    <th>Std_Dev</th>
    <th>Min_Rt</th>
    <th>p50_Rt</th>
    <th>p90_Rt</th>
    <th>p95_Rt</th>
    <th>p99_Rt</th>
    <th>Max_Rt</th>
    <th class="extra-col">Max_Rtt</th>
    <th class="extra-col">AQS</th>
    <th class="extra-col">MQS</th>
    <th class="extra-col">AWT</th>
    <th class="extra-col">MWT</th>
    <th class="extra-col">CPU_T</th>
    <th class="extra-col">CPU</th>
    <th class="extra-col">MEM</th>
  </tr>
'''

benchmark_columns = '''
  <tr>
    <th>Benchmark</th>
    <th>Run Id</th>
    <th>#N</th>
    <th>#F</th>
    <th>Time</th>
    <th>APS</th>
    <th>Avg_Rt</th>
    <th>Std_Dev</th>
    <th>Min_Rt</th>
    <th>p50_Rt</th>
    <th>p90_Rt</th>
    <th>p95_Rt</th>
    <th>p99_Rt</th>
    <th>Max_Rt</th>
    <th class="extra-col">Max_Rtt</th>
    <th class="extra-col">AQS</th>
    <th class="extra-col">MQS</th>
    <th class="extra-col">AWT</th>
    <th class="extra-col">MWT</th>
    <th class="extra-col">CPU_T</th>
    <th class="extra-col">CPU</th>
    <th class="extra-col">MEM</th>
  </tr>
'''

benchmark_header = '''
  <tr>
    <td>%s</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td class="extra-col"></td>
    <td class="extra-col"></td>
    <td class="extra-col"></td>
    <td class="extra-col"></td>
    <td class="extra-col"></td>
    <td class="extra-col"></td>
    <td class="extra-col"></td>
    <td class="extra-col"></td>
  </tr>
'''

benchmark_empty_row = '''
  <tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td class="extra-col">&nbsp;</td>
    <td class="extra-col">&nbsp;</td>
    <td class="extra-col">&nbsp;</td>
    <td class="extra-col">&nbsp;</td>
    <td class="extra-col">&nbsp;</td>
    <td class="extra-col">&nbsp;</td>
    <td class="extra-col">&nbsp;</td>
    <td class="extra-col">&nbsp;</td>
  </tr>
'''

benchmark_detail_row = '''
  <tr>
    <td>%s</td>
    <td>%d</td>
    <td>%d</td>
    <td>%d</td>
    <td>%s</td>
    <td>%.1f</td>
    <td>%s</td>
    <td>%s</td>
    <td>%s</td>
    <td>%s</td>
    <td>%s</td>
    <td>%s</td>
    <td>%s</td>
    <td>%s</td>
    <td class="extra-col">%s</td>
    <td class="extra-col">%.1f</td>
    <td class="extra-col">%d</td>
    <td class="extra-col">%s</td>
    <td class="extra-col">%s</td>
    <td class="extra-col">%s</td>
    <td class="extra-col">%.1f</td>
    <td class="extra-col">%.1f</td>
  </tr>
'''

benchmark_summary_row = '''
  <tr>
    <td>%s</td>
    <td>%s</td>
    <td><b>%d</b></td>
    <td><b><tag1>%d</tag1></b></td>
    <td><b>%s</b></td>
    <td><b><tag2>%.1f</tag2></b></td>
    <td><b>%s</b></td>
    <td><b>%s</b></td>
    <td><b>%s</b></td>
    <td><b>%s</b></td>
    <td><b>%s</b></td>
    <td><b>%s</b></td>
    <td><b>%s</b></td>
    <td><b>%s</b></td>
    <td class="extra-col"><b>%s</b></td>
    <td class="extra-col"><b>%.1f</b></td>
    <td class="extra-col"><b>%d</b></td>
    <td class="extra-col"><b>%s</b></td>
    <td class="extra-col"><b>%s</b></td>
    <td class="extra-col"><b>%s</b></td>
    <td class="extra-col"><b>%.1f</b></td>
    <td class="extra-col"><b>%.1f</b></td>
  </tr>
'''

trailer = '''
</table>

    <script>
        const btn = document.getElementById('toggleBtn');
        const table = document.getElementById('dataTable');

        btn.addEventListener('click', () => {
            // Toggle the CSS class
            table.classList.toggle('expanded');
            
            // Toggle the button text
            if (table.classList.contains('expanded')) {
                btn.textContent = 'Hide Detailed Columns';
            } else {
                btn.textContent = 'Show All Columns';
            }
        });
    </script>
    
</body>
</html>
'''

charts_html = '''
<h2>Actions per Second</h2>
  <div id="main" style="width: 100%;height:400px;"></div>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/echarts/6.0.0/echarts.min.js"></script>
  <script src="__JS_T_CHART__"></script>
<h2>Response Times (ns)</h2>
  <div id="main_p" style="width: 100%;height:400px;"></div>
  <script src="__JS_P_CHART__"></script>
'''

chart_t_html = '''
var myChart1 = echarts.init(document.getElementById('main'));

var data =  __DATA__;

var option = {
  legend: {},
  tooltip: {
    trigger: 'axis',
  },
  dataset: {
    source:data,
    dimensions: ['timestamp', 'sensor1', 'sensor2'],
  },
  xAxis: { type: 'time' },
  yAxis: { },
  series: [
  {
     name: 'Actions/s',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor1' // refer sensor 1 value 
     }
  },{
     name: 'Failures/s',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor2'
  }
}]
};
myChart1.setOption(option);
'''

chart_p_html = '''
var myChart2 = echarts.init(document.getElementById('main_p'));

var data =  __DATA__;

var option = {
  legend: {},
  tooltip: {
    trigger: 'axis',
  },
  dataset: {
    source:data,
    dimensions: ['timestamp', 'sensor1', 'sensor2', 'sensor3', 'sensor4', 'sensor5', 'sensor6', 'sensor7'],
  },
  xAxis: { type: 'time' },
  yAxis: { },
  series: [
  {
     name: 'Max',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor7' // refer sensor 1 value
     }
  },{
     name: 'p99',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor6' // refer sensor 1 value
     }
  },{
     name: 'p95',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor5' // refer sensor 1 value
     }

  },{
     name: 'p90',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor4' // refer sensor 1 value
     }

  },{
     name: 'p50',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor3' // refer sensor 1 value
     }

  },{
     name: 'Avg',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor2'
     }
  },{
     name: 'Min',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor1'
  }
}]
};
myChart2.setOption(option);

window.onresize = function() {
  myChart1.resize();
  myChart2.resize();
};
'''

class Summary:
    def __init__(self):
        self.num_actions = 0
        self.num_failed = 0
        self.duration = 0.0
        self.min_rt = 1000000000.0
        self.max_rt = 0.0
        self.max_rt_ts = ""
        self.mem = 0.0
        self.cpu = 0.0
        self.max_awt = 0.0
        self.max_wt = 0.0
        self.avg_qs = 0.0
        self.max_qs = 0
        self.name = ""
        self.cpu_time_ns = 0
        self.chart_t_list = []   # ['2018-04-10T20:40:33.100', 1100, 0]
        self.chart_p_list = []   # ['2018-04-10T20:40:33Z', 1, 5, 10, 20, 25, 30]
        self.aps_count = 0.0
        self.aps_target_sum = 0.0
        self.aps_min = None
        self.aps_max = None

def createReport(filename):

    def formatTime(timeNanos):
        if timeNanos < 1000.0:
            return "%.1f ns"%(timeNanos)
        elif timeNanos < 1000000.0:
            return "%.1f us"%(timeNanos/1000.0)
        elif timeNanos < 1000000000.0:
            return "%.1f ms"%(timeNanos/1000000.0)
        else:
            return "%.1f s"%(timeNanos/1000000000.0)

    cwd = os.getcwd()
    if os.path.isdir("build/reports/tulip"):
        os.chdir("build/reports/tulip")

    print("\nOutput filename = " + filename)

    config_filename = ""
    config_filename2 = ""

    jhh = {}
    jhq = {}
    jss = {}

    print_detail_rows = True

    global name2s
    global name2s_list
    global benchmark_id
    name2s = ""
    name2s_list = []
    benchmark_id = 0

    sm = None
    jh = Histogram(3)
    llq_jh = LlqHistogram()
    fileObj = open(filename)
    jb = json.load(fileObj, object_pairs_hook=OrderedDict)
    version = jb['version']
    desc1 = 'Tulip ' + version
    rb = jb["results"]

    report_html_fn = jb["config"]["actions"]["report_filename"]
    report_html_fh = open(report_html_fn, "w+")

    report_json_fn = os.path.splitext(report_html_fn)[0] + ".json"
    report_json_fh = open(report_json_fn, "w+")
    report_json_fh.write("{\n")
    report_json_fh.write('  "config": ' + json.dumps(jb["config"]) + ',\n')
    report_json_fh.write('  "benchmarks": {\n')

    global report_dn
    report_dn = os.path.splitext(report_html_fn)[0]
    try:
        os.makedirs(report_dn)
    except:
        pass

    config_filename = os.path.join(report_dn, report_dn + '_c.html')
    config_filename2 = os.path.basename(config_filename)

    def odir(filename):
        global report_dn
        return os.path.join(os.getcwd(), report_dn, filename)

    def odirHtml(filename):
        global report_dn
        return report_dn + "/" + filename

    def name_to_href(name):
        href = '_' + re.sub(r'\W', '_', name.lower())
        hfile = os.path.splitext(config_filename2)[0] + ".html"
        return "<a href='%s#%s'>"%(hfile, href) + name + "</a>"

    if len(config_filename2) > 0:
        desc2 = "<a href='%s'>"%(os.path.splitext(config_filename2)[0] + ".html")
    else:
        desc2 = ""

    if len(config_filename2) > 0:
        desc2 += jb["config"]["actions"]["description"] + "</a> / " + jb["timestamp"][:-3]
    else:
        desc2 += jb["config"]["actions"]["description"] + " / " + jb["timestamp"][:-3]

    print("Report filename = " + report_html_fn)

    def printf(s):
        report_html_fh.write(s)

    def actionName(s):
        return s.split(",")[0].strip()

    def actionDesc(s):
        try:
            return s.split(",")[1].strip()
        except:
            return ""

    def str_from_cpu_time_ns(v_ns):
        r = str(datetime.timedelta(seconds=v_ns/1000000000.0))  #[:-5]
        if '.' in r:
            r = r[:-5]
        return r

    def print_global_summary():
        global name2s
        global name2s_list
        global benchmark_id

        avg_aps = 0.0 if sm.name in ["onStart", "onStop"] else sm.num_actions/sm.duration
        if sm.name in ["onStart", "onStop"]:
            cpu_t = "0:00:00"
            sm.cpu = 0.0
        else:
            cpu_t = str_from_cpu_time_ns(sm.cpu_time_ns)

        statsFilename = '%s_%d.html'%(odir(report_html_fn.split('.')[0]),benchmark_id)
        statsFilenameHtml = '%s_%d.html'%(odirHtml(report_html_fn.split('.')[0]),benchmark_id)
        text = "<a href='%s'>%s</a>"%(statsFilenameHtml,"[Summary]")
        printStream = PrintStream(statsFilename)
        printStream.print(summary_html_1)
        printStream.println()
        printStream.print('    function drawInitialChart() {')
        printStream.println()
        printStream.print('        // Connect the choose files button:')
        printStream.println()
        printStream.print("        document.getElementById('files').addEventListener('change', handleFileSelect, false);")
        printStream.println()
        printStream.println()
        printStream.print('        // Load some static example data:')
        printStream.println()
        idx = 0
        actionsString = ""
        histosString = ""
        for key in jss.keys():
            idx += 1
            smx = jss[key]
            jhx = jhh[key]
            llq = jhq[key]
            if len(actionsString) > 0:
                actionsString += ', '
                histosString += ', '
            actionsString += "'A%s'"%(key)
            histosString += "data%dStr"%(idx)
            printStream.print('    var data%dStr = document.querySelector("div#data_%d").innerHTML.trim();'%(idx,idx))
            printStream.println()
        if False: # if idx > 1: # Summary graph line (disabled)
            actionsString += ', ' + "'S'"
            histosString += ', ' + "data%dStr"%(0)
            printStream.print('    var data%dStr = document.querySelector("div#data_%d").innerHTML.trim();'%(0,0))
            printStream.println()
        printStream.print('    var histos = [%s];'%(histosString))
        printStream.println()
        printStream.print('    var names = [%s];'%(actionsString))
        printStream.println()

        printStream.print('    setChartData(names, histos);')
        printStream.println()
        printStream.print('    drawChart();')
        printStream.println()
        printStream.print('    }')
        printStream.println()
        tChartFilename = '%s_%d_t.js'%(odir(report_html_fn.split('.')[0]),benchmark_id)
        tChartFilenameHtml = '%s_%d_t.js'%(odirHtml(report_html_fn.split('.')[0]),benchmark_id)
        pChartFilename = '%s_%d_p.js'%(odir(report_html_fn.split('.')[0]),benchmark_id)
        pChartFilenameHtml = '%s_%d_p.js'%(odirHtml(report_html_fn.split('.')[0]),benchmark_id)
        printStream.print(
            summary_html_2
                .replace("__CHARTS_TEXT__",
                    charts_html
                         .replace("__JS_T_CHART__",os.path.basename(tChartFilenameHtml))
                         .replace("__JS_P_CHART__",os.path.basename(pChartFilenameHtml)))
                )
        printStream.println()

        idx = 0
        for key in jss.keys():
            idx += 1
            smx = jss[key]
            jhx = jhh[key]
            printStream.print('<div id="data_%d" class="histo">'%(idx))
            printStream.println()
            jhx.outputPercentileDistribution(printStream, 1000000.0)
            printStream.print('</div>')
            printStream.println()

        printStream.print('<div id="data_0" class="histo">')
        printStream.println()
        jh.outputPercentileDistribution(printStream, 1000000.0)
        printStream.print('</div>')
        printStream.println()

        printStream.print(summary_html_table_2)
        print_llq_histogram_table(printStream, llq_jh)

        printStream.print(summary_html_table_1)
        print_percentile_table(printStream,jh)

        printStream.println()
        printStream.flush()
        printStream.close()

        # Actions/s chart
        #chart_t_list = [
        #    ['2018-04-10T20:40:33.100', 1100, 0],
        #    ['2018-04-10T20:40:53.200', 1002, 0],
        #    ['2018-04-10T20:41:03.300', 1104, 0],
        #    ['2018-04-10T20:44:03.400', 1205.5, 0],
        #    ['2018-04-10T20:45:03.500', 1306, 0]
        #]
        chart_p_list = [
            ['2018-04-10T20:40:33Z', 1, 5, 6, 10, 20, 25, 30 ],
            ['2018-04-10T20:40:53Z', 2, 6, 7, 15, 25, 30, 35],
            ['2018-04-10T20:41:03Z', 2, 5, 6, 20, 30, 35, 40],
            ['2018-04-10T20:44:03Z', 3, 7, 8, 12, 22, 30, 32],
            ['2018-04-10T20:45:03Z', 2, 10, 11, 13, 23, 30, 33]
        ]
        printStream = PrintStream(tChartFilename)
        printStream.print(chart_t_html.replace("__DATA__",("%s"%(sm.chart_t_list)).replace("u'","'")))
        printStream.println()
        printStream.flush()
        printStream.close()

        # Response Time (ms) chart
        printStream = PrintStream(pChartFilename)
        printStream.print(chart_p_html.replace("__DATA__",("%s"%(sm.chart_p_list)).replace("u'","'")))
        printStream.println()
        printStream.flush()
        printStream.close()

        if True:
            rd = {}
            rd["num_actions"] = sm.num_actions
            rd["num_failed"] = sm.num_failed
            rd["duration"] = str(datetime.timedelta(seconds=int(sm.duration)))
            rd["avg_aps"] = avg_aps
            rd["aps_target_rate"] = sm.aps_target_sum / sm.aps_count
            rd["min_aps"] = sm.aps_min
            rd["max_aps"] = sm.aps_max
            rd["avg_rt"] = jh.getMean()
            rd["std_dev"] = jh.getStdDeviation()
            rd["min_rt"] = sm.min_rt
            rd["p50_rt"] = jh.getValueAtPercentile(50.0)
            rd["p90_rt"] = jh.getValueAtPercentile(90.0)
            rd["p95_rt"] = jh.getValueAtPercentile(95.0)
            rd["p99_rt"] = jh.getValueAtPercentile(99.0)
            rd["max_rt"] = sm.max_rt
            rd["max_rtt"] = sm.max_rt_ts.replace("_","T")
            rd["AQS"] = sm.avg_qs
            rd["MQS"] = sm.max_qs
            rd["AWT"] = sm.max_awt
            rd["MWT"] = sm.max_wt
            rd["CPU_T"] = cpu_t
            rd["CPU"] = sm.cpu
            rd["MEM"] = sm.mem

            report_json_fh.write('        ,"summary": %s\n'%(json.dumps(rd)))

        html = benchmark_summary_row%(
            name2s,
            text,
            sm.num_actions,
            sm.num_failed,
            str(datetime.timedelta(seconds=int(sm.duration))),
            avg_aps,
            formatTime(jh.getMean()),
            formatTime(jh.getStdDeviation()),
            formatTime(sm.min_rt),
            formatTime(jh.getValueAtPercentile(50.0)),
            formatTime(jh.getValueAtPercentile(90.0)),
            formatTime(jh.getValueAtPercentile(95.0)),
            formatTime(jh.getValueAtPercentile(99.0)),
            formatTime(sm.max_rt),
            sm.max_rt_ts[8:],
            sm.avg_qs,
            sm.max_qs,
            formatTime(sm.max_awt),
            formatTime(sm.max_wt),
            cpu_t,
            sm.cpu,
            sm.mem)
        if not print_detail_rows:
            html = html.replace("<b>","")
            html = html.replace("</b>","")
        # Validation: #F
        if sm.num_failed > 0:
            html = html.replace("<tag1>","<mark>")
            html = html.replace("</tag1>","</mark>")
        else:
            html = html.replace("<tag1>","")
            html = html.replace("</tag1>","")
        # Validation: Avg_APS
        #print(sm.name)
        target_aps = rd["aps_target_rate"]
        if target_aps > 0.0:
            delta_percentage_aps = 100.0*abs(target_aps-avg_aps)/target_aps
            if delta_percentage_aps > 1.5:
                html = html.replace("<tag2>","<mark>")
                html = html.replace("</tag2>","</mark>")
            else:
                html = html.replace("<tag2>","")
                html = html.replace("</tag2>","")
        else:
            html = html.replace("<tag2>","")
            html = html.replace("</tag2>","")
        printf(html)
        if len(name2s_list) > 0:
            name2s = name2s_list[0]
            del name2s_list[0]

    def print_llq_histogram_table(printStream, llq_histogram):
        printStream.println('<table style="width:800px">')
        printStream.println(llq_histogram.toHtmlString())
        printStream.println("</table>")

    def print_percentile_table(printStream, jhx):
        bos = ByteArrayOutputStream()
        ox = PrintStream(bos)
        jhx.outputPercentileDistribution(ox, 1000000.0)
        ox.flush()
        ox.close()
        ptext = bos.toString()
        lines = ptext.split('\n')
        tcount = jhx.getTotalCount()
        header = True
        rlist = []
        for line in lines:
            if len(line) == 0:
                continue
            if line[0] != "#":
                e = line.split()
                if header:
                    printStream.println('<table style="width:800px">')
                    printStream.println('  <tr>')
                    printStream.println('    <th>%s</th>'%(e[0]))
                    printStream.println('    <th>%s</th>'%(e[1]))
                    printStream.println('    <th>%s</th>'%(e[2]))
                    printStream.println('    <th>%s</th>'%(e[3]))
                    printStream.println('    <th>AboveCount</th>')
                    printStream.println('  </tr>')
                    header = False
                else:
                    if len(e) == 3:
                        e.append('')
                    rlist.append(e)
        rlist.reverse()
        for e in rlist:
            mark = False
            if e[1].startswith("0.9990"):
                mark = True
            if e[1].startswith("0.990"):
                mark = True
            if e[1] == "0.950000000000":
                mark = True
            if e[1] == "0.900000000000":
                mark = True
            if e[1] == "0.750000000000":
                mark = True
            if e[1] == "0.500000000000":
                mark = True
            printStream.println('  <tr>')
            if mark:
                printStream.println('    <td><mark>%s ms</mark></td>'%(e[0]))
                printStream.println('    <td><mark>%s</mark></td>'%(e[1]))
            else:
                printStream.println('    <td>%s ms</td>'%(e[0]))
                printStream.println('    <td>%s</td>'%(e[1]))
            printStream.println('    <td>%s</td>'%(e[2]))
            printStream.println('    <td>%s</td>'%(e[3]))
            printStream.println('    <td>%s</td>'%(tcount-int(e[2])))
            printStream.println('  </tr>')

        printStream.println('  <tr>')
        printStream.println('    <td></td>')
        printStream.println('    <td></td>')
        printStream.println('    <td></td>')
        printStream.println('    <td></td>')
        printStream.println('    <td></td>')
        printStream.println('  </tr>')

        printStream.println('  <tr>')
        printStream.println('    <td>AVG: %s</td>'%(formatTime(jhx.getMean())))
        printStream.println('    <td>SD: %s</td>'%(formatTime(jhx.getStdDeviation())))
        printStream.println('    <td>P90: %s</td>'%(formatTime(jhx.getValueAtPercentile(90))))
        printStream.println('    <td>P95: %s</td>'%(formatTime(jhx.getValueAtPercentile(95))))
        printStream.println('    <td>P99: %s</td>'%(formatTime(jhx.getValueAtPercentile(99))))
        printStream.println('  </tr>')

        printStream.println("</table>")

    def print_action_summary():
        global name2s
        global name2s_list
        global benchmark_id
        page_id = 0
        num_keys = len(jss.keys())
        for key in jss.keys():
            smx = jss[key]
            jhx = jhh[key]
            llq = jhq[key]
            if jb["config"]["actions"]["user_actions"].has_key(key):
                text = "[%s.%s]"%(key, actionName(jb["config"]["actions"]["user_actions"][key]))
            else:
                text = "[%s]"%(key)
            statsFilename = '%s_%d_%d.html'%(odir(report_html_fn.split('.')[0]),benchmark_id,page_id)
            statsFilenameHtml = '%s_%d_%d.html'%(odirHtml(report_html_fn.split('.')[0]),benchmark_id,page_id)
            statsFilenamePrev = '%s_%d_%d.html'%(report_html_fn.split('.')[0],benchmark_id,abs(page_id-1))
            statsFilenameNext = '%s_%d_%d.html'%(report_html_fn.split('.')[0],benchmark_id,page_id+1)
            text = "<a href='%s'>%s</a>"%(statsFilenameHtml,text)
            printStream = PrintStream(statsFilename)
            printStream.println("<html lang=\"en\">")
            printStream.println("<style>")
            printStream.println("table, th, td {")
            printStream.println("  border:1px solid black; font-size:16px; text-align: center;")
            printStream.println("}")
            printStream.println("mark {")
            printStream.println("  background-color: LightGray;")
            printStream.println("  color: black;")
            printStream.println("}")
            printStream.println("</style>")
            printStream.println("<body>")
            printStream.println('<a href="../%s">Up</a>'%(report_html_fn))
            if (page_id == 0) and (num_keys == 1):
                printStream.println('<a href="#">Prev</a>')
                printStream.println('<a href="#">Next</a>')
            elif (page_id == 0) and (num_keys > 1):
                printStream.println('<a href="#">Prev</a>')
                printStream.println('<a href="%s">Next</a>'%(statsFilenameNext))
            elif page_id+1 == num_keys:
                printStream.println('<a href="%s">Prev</a>'%(statsFilenamePrev))
                printStream.println('<a href="#">Next</a>')
            else:
                printStream.println('<a href="%s">Prev</a>'%(statsFilenamePrev))
                printStream.println('<a href="%s">Next</a>'%(statsFilenameNext))

            if jb["config"]["actions"]["user_actions"].has_key(key):
                desc = "[%s.%s]"%(key, actionName(jb["config"]["actions"]["user_actions"][key]))
            else:
                desc = "[A%s]"%(key)

            printStream.println("<h2>%s Percentile Response Time Distribution (LLQ)</h2>"%(desc))
            print_llq_histogram_table(printStream, llq)

            printStream.println("<h2>%s Percentile Response Time Distribution (HDR)</h2>"%(desc))
            print_percentile_table(printStream,jhx)

            printStream.println("</body>")
            printStream.println("</html>")
            printStream.flush()
            printStream.close()
            avg_aps = 0.0 if smx.name in ["onStart", "onStop"] else smx.num_actions/smx.duration
            if smx.name in ["onStart", "onStop"]:
                cpu_t = "0:00:00"
                smx.cpu = 0.0
            else:
                cpu_t = str_from_cpu_time_ns(smx.cpu_time_ns)

            if True:
                rd = {}
                rd["num_actions"] = smx.num_actions
                rd["num_failed"] = smx.num_failed
                rd["duration"] = str(datetime.timedelta(seconds=int(sm.duration)))
                rd["avg_aps"] = avg_aps
                rd["avg_rt"] = jhx.getMean()
                rd["std_dev"] = jhx.getStdDeviation()
                rd["min_rt"] = smx.min_rt
                rd["p50_rt"] = jhx.getValueAtPercentile(50.0)
                rd["p90_rt"] = jhx.getValueAtPercentile(90.0)
                rd["p95_rt"] = jhx.getValueAtPercentile(95.0)
                rd["p99_rt"] = jhx.getValueAtPercentile(99.0)
                rd["max_rt"] = smx.max_rt
                rd["max_rtt"] = smx.max_rt_ts.replace("_","T")
                if page_id == 0:
                    report_json_fh.write('      }\n')
                    report_json_fh.write('      ,"actions": {\n')
                    report_json_fh.write('         "%s": %s\n'%(key,json.dumps(rd)))
                else:
                    report_json_fh.write('        ,"%s": %s\n'%(key,json.dumps(rd)))

            html = benchmark_summary_row%(
                name2s,
                text,
                smx.num_actions,
                smx.num_failed,
                str(datetime.timedelta(seconds=int(sm.duration))),
                avg_aps,
                formatTime(jhx.getMean()),
                formatTime(jhx.getStdDeviation()),
                formatTime(smx.min_rt),
                formatTime(jhx.getValueAtPercentile(50.0)),
                formatTime(jhx.getValueAtPercentile(90.0)),
                formatTime(jhx.getValueAtPercentile(95.0)),
                formatTime(jhx.getValueAtPercentile(99.0)),
                formatTime(smx.max_rt),
                smx.max_rt_ts[8:],
                smx.avg_qs,
                smx.max_qs,
                formatTime(smx.max_awt),
                formatTime(smx.max_wt),
                cpu_t,
                smx.cpu,
                smx.mem)
            if not print_detail_rows:
                html = html.replace("<b>","")
                html = html.replace("</b>","")
            # Remove tag1
            html = html.replace("<tag1>","")
            html = html.replace("</tag1>","")
            # Remove tag2
            html = html.replace("<tag2>","")
            html = html.replace("</tag2>","")

            printf(html)
            if len(name2s_list) > 0:
                name2s = name2s_list[0]
                del name2s_list[0]

            page_id += 1
        #report_json_fh.write('      }\n')

    printf(header.replace("__DESC1__", desc1).replace("__DESC2__", desc2))
    json_bm_names = {}
    prev_row_id = 0
    for e in rb:
        current_row_id = int(e["row_id"])
        if current_row_id <= prev_row_id:
            if sm is not None:
                print_action_summary()
                print_global_summary()
                printf(benchmark_empty_row)
                printf(benchmark_columns)
            sm = Summary()
            sm.name = e["bm_name"]
            jh.reset()
            llq_jh.reset()
            benchmark_id += 1
            jhh = {}
            jhq = {}
            jss = {}
            printf(benchmark_header%(name_to_href(e["bm_name"])))
            if len(e["workflow_name"]) > 0:
                name2s_list = ["u:%d, a:%d, t:%d"%(e["num_users"],e["num_tasks"],e["num_threads"]), "w:%s"%(e["workflow_name"]), "c:%d"%(e["context_id"]) ,""]
            else:
                name2s_list = ["u:%d, a:%d, t:%d"%(e["num_users"],e["num_tasks"],e["num_threads"]), "c:%d"%(e["context_id"]),""]
            name2s = name2s_list[0]
            del name2s_list[0]
        ht = Histogram.fromString(e["hdr_histogram_rt"])
        jh.add(ht)
        llq_jh.add(ht)
        p_mem = 100.0 * e["jvm_memory_used"] / e["jvm_memory_maximum"]
        p_cpu = e["process_cpu_utilization"]
        if e["bm_name"] in ["onStart", "onStop"]:
            cpu_t = "0:00:00"
            p_cpu = 0.0
        else:
            cpu_t = str_from_cpu_time_ns(e["process_cpu_time_ns"])
            p_cpu = e["process_cpu_utilization"]
        if not json_bm_names.has_key(e["bm_name"]):
            json_bm_names[e["bm_name"]] = 1
            if len(json_bm_names.keys()) > 1:
                report_json_fh.write('      }\n')
                report_json_fh.write('    },\n')
            report_json_fh.write('    "%s": {\n'%(e["bm_name"]))
            context = {}
            context["name"] = e["context_name"]
            context["num_users"] = e["num_users"]
            context["num_threads"] = e["num_threads"]
            report_json_fh.write('      "context": %s,'%(json.dumps(context)))
            report_json_fh.write('      "results": {\n')

        if True:
            rd = {}
            rd["num_actions"] = e["num_actions"]
            rd["num_failed"] = e["num_failed"]
            rd["duration"] = str(datetime.timedelta(seconds=int(e["duration"])))
            rd["avg_aps"] = 0.0 if e["bm_name"] in ["onStart", "onStop"] else e["avg_aps"]
            rd["avg_rt"] = e["avg_rt"]
            rd["std_dev"] = ht.getStdDeviation()
            rd["min_rt"] = e["min_rt"]
            rd["p50_rt"] = e["percentiles_rt"]["50.0"]
            rd["p90_rt"] = e["percentiles_rt"]["90.0"]
            rd["p95_rt"] = e["percentiles_rt"]["95.0"]
            rd["p99_rt"] = e["percentiles_rt"]["99.0"]
            rd["max_rt"] = e["max_rt"]
            rd["max_rtt"] = e["max_rt_ts"].replace("_","T")
            rd["AQS"] = e["avg_wthread_qsize"]
            rd["MQS"] = e["max_wthread_qsize"]
            rd["AWT"] = e["avg_wt"]
            rd["MWT"] = e["max_wt"]
            rd["CPU_T"] = cpu_t
            rd["CPU"] = p_cpu
            rd["MEM"] = p_mem
            if e["row_id"]+1 == 1:
                report_json_fh.write('         "%d": %s\n'%(e["row_id"]+1,json.dumps(rd)))
            else:
                report_json_fh.write('        ,"%d": %s\n'%(e["row_id"]+1,json.dumps(rd)))
        if print_detail_rows:
            printf(benchmark_detail_row%( \
                name2s,
                e["row_id"]+1,
                e["num_actions"],
                e["num_failed"],
                str(datetime.timedelta(seconds=int(e["duration"]))),
                0.0 if e["bm_name"] in ["onStart", "onStop"] else e["avg_aps"],
                formatTime(e["avg_rt"]),
                formatTime(ht.getStdDeviation()),
                formatTime(e["min_rt"]),
                formatTime(e["percentiles_rt"]["50.0"]),
                formatTime(e["percentiles_rt"]["90.0"]),
                formatTime(e["percentiles_rt"]["95.0"]),
                formatTime(e["percentiles_rt"]["99.0"]),
                formatTime(e["max_rt"]),
                e["max_rt_ts"][8:],
                e["avg_wthread_qsize"],
                e["max_wthread_qsize"],
                formatTime(e["avg_wt"]),
                formatTime(e["max_wt"]),
                cpu_t,
                p_cpu,
                p_mem
                ))
            if len(name2s_list) > 0:
                name2s = name2s_list[0]
                del name2s_list[0]
        if sm.min_rt > e["min_rt"]:
            sm.min_rt = e["min_rt"]
        if sm.max_rt < e["max_rt"]:
            sm.max_rt = e["max_rt"]
            sm.max_rt_ts = e["max_rt_ts"]
        sm.num_actions += e["num_actions"]
        sm.num_failed += e["num_failed"]
        sm.duration += e["duration"]
        if sm.avg_qs < e["avg_wthread_qsize"]:
            sm.avg_qs = e["avg_wthread_qsize"]
        if sm.max_qs < e["max_wthread_qsize"]:
            sm.max_qs = e["max_wthread_qsize"]
        if sm.max_awt < e["avg_wt"]:
            sm.max_awt = e["avg_wt"]
        if sm.max_wt < e["max_wt"]:
            sm.max_wt = e["max_wt"]
        if sm.mem < p_mem:
            sm.mem = p_mem
        if sm.cpu < p_cpu:
            sm.cpu = p_cpu
        sm.cpu_time_ns += e["process_cpu_time_ns"]
        sm.aps_count += 1.0
        sm.aps_target_sum += e["aps_target_rate"]

        if sm.aps_min == None:
            sm.aps_min = e["avg_aps"]
        elif sm.aps_min > e["avg_aps"]:
            sm.aps_min = e["avg_aps"]
        if sm.aps_max == None:
            sm.aps_max = e["avg_aps"]
        elif sm.aps_max < e["avg_aps"]:
            sm.aps_max = e["avg_aps"]

        if len(sm.chart_t_list) == -1:
            sm.chart_t_list.append(
            [
                '%s'%(e["test_begin"].replace("_", "T")),
                float("%.1f"%(e["avg_aps"])),
                float("%.3f"%(e["num_failed"]/e["duration"]))
            ])

        sm.chart_t_list.append(
            [
                '%s'%(e["test_end"].replace("_", "T")),
                float("%.1f"%(e["avg_aps"])),
                float("%.3f"%(e["num_failed"]/e["duration"]))
             ])

        # ['2018-04-10T20:40:33Z', 1, 5, 10, 20, 25, 30 ]
        if len(sm.chart_p_list) == -1:
            sm.chart_p_list.append(
                [
                    '%s'%(e["test_begin"].replace("_", "T")),
                    float("%.1f"%(e["min_rt"])),
                    float("%.1f"%(e["avg_rt"])),
                    float("%.1f"%(ht.getValueAtPercentile(50.0))),
                    float("%.1f"%(ht.getValueAtPercentile(90.0))),
                    float("%.1f"%(ht.getValueAtPercentile(95.0))),
                    float("%.1f"%(ht.getValueAtPercentile(99.0))),
                    float("%.1f"%(e["max_rt"]))
                ])

        sm.chart_p_list.append(
            [
                '%s'%(e["test_end"].replace("_", "T")),
                float("%.1f"%(e["min_rt"])),
                float("%.1f"%(e["avg_rt"])),
                float("%.1f"%(ht.getValueAtPercentile(50.0))),
                float("%.1f"%(ht.getValueAtPercentile(90.0))),
                float("%.1f"%(ht.getValueAtPercentile(95.0))),
                float("%.1f"%(ht.getValueAtPercentile(99.0))),
                float("%.1f"%(e["max_rt"]))
            ])

        # jhh ...
        for key in e["user_actions"].keys():
            ar = e["user_actions"][key]
            htt = Histogram.fromString(ar["hdr_histogram_rt"])
            llq = LlqHistogram()
            llq.add(htt)
            #print(ar["name"] + " - " + "%.3f"%(htt.getMean()/1000.0))
            if jhh.has_key(key):
                jhh[key].add(htt)
            else:
                jhh[key] = Histogram(3)
                jhh[key].add(htt)
            if jhq.has_key(key):
                jhq[key].add(llq)
            else:
                jhq[key] = LlqHistogram()
                jhq[key].add(llq)
            #print(ar["name"] + " - " + "%.3f"%(jhh[key].getMean()/1000.0) + " - %d"%(jhh[key].getTotalCount()))

        # jss ...
        for key in e["user_actions"].keys():
            ar = e["user_actions"][key]
            if jss.has_key(key):
                smx = jss[key]
            else:
                smx = jss[key] = Summary()
                smx.name = e["bm_name"]

            if smx.min_rt > ar["min_rt"]:
                smx.min_rt = ar["min_rt"]
            if smx.max_rt < ar["max_rt"]:
                smx.max_rt = ar["max_rt"]
                smx.max_rt_ts = ar["max_rt_ts"]
            smx.num_actions += ar["num_actions"]
            smx.num_failed += ar["num_failed"]

            smx.duration += e["duration"]
            if smx.avg_qs < e["avg_wthread_qsize"]:
                smx.avg_qs = e["avg_wthread_qsize"]
            if smx.max_qs < e["max_wthread_qsize"]:
                smx.max_qs = e["max_wthread_qsize"]
            if smx.max_awt < e["avg_wt"]:
                smx.max_awt = e["avg_wt"]
            if smx.max_wt < e["max_wt"]:
                smx.max_wt = e["max_wt"]
            if smx.mem < p_mem:
                smx.mem = p_mem
            if smx.cpu < p_cpu:
                smx.cpu = p_cpu
            smx.cpu_time_ns += e["process_cpu_time_ns"]

    print_action_summary()
    print_global_summary()
    printf(trailer)

    report_html_fh.close()

    report_json_fh.write("      }\n")
    report_json_fh.write("    }\n")
    report_json_fh.write("  }\n")
    report_json_fh.write("}\n")
    report_json_fh.close()

    config_json = jb["config"]
    java_json = jb["java"]
    createConfigReport(config_json, java_json, config_filename)

    os.chdir(cwd)

adoc_header = '''= __DESCRIPTION__
:toc: left
:sectnums:
:source-highlighter: rouge

// :source-highlighter: highlight.js
// :plantuml: http://localhost:8080/plantuml
// :plantuml-fetch-diagram: true
// :plantuml-size-limit: 8192
// :plantuml: plantuml.com/plantuml
// :diagram-server-url: https://kroki.io/
// :diagram-server-type: kroki_io

== Actions

=== Configuration

[%header,cols="1a,4a"]
|===
| id | value
'''

def createConfigReport(config_json, java_json, config_filename):
    #print(config_filename)

    def printf(s):
        report_fh.write(s)

    def actionName(s):
        return s.split(",")[0].strip()

    def actionDesc(s):
        try:
            return s.split(",")[1].strip()
        except:
            return ""

    def generate_table1(e):
        printf("| *" + e + "*\n")
        printf("|\n")
        printf('[%header,cols="1a,3a"]\n')
        printf('!===\n')
        printf('! id ! value \n')
        for k in jb['actions'][e].keys():
            printf('! *' + k + '* ')
            printf('! ' + str(jb['actions'][e][k]) + '\n')
        printf('!===\n')

    def generate_table2(e):
        printf("| *" + e + "*\n")
        printf("|\n")
        printf('[%header,cols="2a,2a,4a"]\n')
        printf('!===\n')
        printf('! id ! value ! description\n')
        for k in jb['actions'][e].keys():
            printf('! *' + k + '* ')
            printf('! ' + actionName(str(jb['actions'][e][k])))
            printf('! ' + actionDesc(str(jb['actions'][e][k])) + '\n')
        printf('!===\n')

    def generate_workflow():
        diagId = -1
        def name_to_id(s):
            if s in '-':
                return 0
            return int(s)
        def action_name(s):
            if s in jb['actions']['user_actions'].keys():
                return actionName(jb['actions']['user_actions'][s])
            return '<unknown>'

        #print(jb.keys())
        #print(jb["workflows"].keys())
        for wn in jb["workflows"].keys():
            diagId += 1
            printf("\n")
            printf("[[%s]]\n"%(wn))
            printf("=== " + "%s\n"%(wn))
            printf("\n")
            printf('[%header,cols="1a,1a"]\n')
            printf('|===\n')
            printf('| Workflow Diagram | Specification\n')
            printf('|')
            printf("[plantuml,wfd%d,svg]"%(diagId) + '\n')
            printf('----\n')
            printf('@startuml\n')
            #printf('title %s\n'%(wn))
            for sname in jb['workflows'][wn].keys():
                if sname in ['-']:
                    printf('state "-" as A0\n')
                    continue
                sid = int(sname)
                printf('state "Action %d" as A%d\n'%(sid,sid))
                printf('A%d: <%s>\n'%(sid,action_name(sname)))
                printf('\n')
            for sname in jb['workflows'][wn].keys():
                if sname in ['*']:
                    continue
                if sname in ['-']:
                    mid = 0
                else:
                    mid = int(sname)
                for k in jb['workflows'][wn][sname].keys():
                    nid = name_to_id(k)
                    fv = jb['workflows'][wn][sname][k]
                    printf('A%d --> A%d: %.2f\n'%(mid,nid,fv))
            printf('@enduml\n')
            printf('----\n')
            printf('| \n')
            printf('[source,json]\n')
            printf('----\n')
            printf('%s\n'%(json.dumps(jb['workflows'][wn], indent=4)))
            printf('----\n')
            printf('|===\n')

    #print("\nConfig filename = " + config_filename)

    # .json/.jsonc -> .adoc
    f_ext = os.path.splitext(config_filename)[1]
    report_fn = config_filename[:-len(f_ext)]+".adoc"
    report_fn = os.path.basename(report_fn)
    report_fh = open(report_fn, "w+")

    # Restore JSON from String
    jb = config_json
    jsonWithoutComments = None

    # print header
    printf(adoc_header.replace("__DESCRIPTION__", jb['actions']['description']))

    # Actions
    for e in jb['actions'].keys():
        if e in ['user_params', 'user_actions']:
            if e == 'user_params':
                generate_table1(e)
            if e == 'user_actions':
                generate_table2(e)
            continue
        # | *description*
        # | Micro-benchmarks
        printf("| *" + e + '*\n')
        printf("| " + jb['actions'][e] + '\n')
    printf("|===" + '\n')

    # Workflows
    if "workflows" in jb.keys():
        printf("\n")
        printf("== Workflows \n")
        generate_workflow()

    # Context Data
    printf("\n")
    printf("== Contexts\n")
    printf("\n")

    for k in jb['contexts'].keys():
        c = jb['contexts'][k]
        printf('=== %s'%(k) + '\n')
        printf('\n')
        printf('[%header,cols="1a,2a"]\n')
        printf('|===\n')
        printf('| id | value \n')
        if "enabled" in c.keys():
            printf('| *enabled* | %s\n'%(c["enabled"]))
        else:
            printf('| *enabled* | True\n')
        printf('| *num_users*   | %d\n'%(c["num_users"]))
        if c.has_key("num_tasks"):
            printf('| *num_tasks* | %d\n'%(c["num_tasks"]))
        if c.has_key("num_threads"):
            printf('| *num_threads* | %d\n'%(c["num_threads"]))
        e0 = "user_params"
        if e0 in c.keys():
            printf("| *" + e0 + "*\n")
            printf("|\n")
            printf('[%header,cols="1a,3a"]\n')
            printf('!===\n')
            printf('! id ! value \n')
            for k0 in c[e0].keys():
                printf('! *' + k0 + '* ')
                printf('! ' + str(c[e0][k0]) + '\n')
            printf('!===\n')
        if c.has_key("workload_model"):
            printf('| *workload_model* | %s\n'%(c["workload_model"]))
        else:
            printf('| *workload_model* | %s\n' % "closed")
        printf("|===\n")

    # Benchmarks Data
    printf("\n")
    printf("== Benchmarks\n")
    for k in jb['benchmarks'].keys():
        b = jb['benchmarks'][k]
        printf("\n")
        printf('=== %s'%(k) + '\n')
        printf("\n")
        printf('[%header,cols="1a,2a"]\n')
        printf("|===\n")
        printf("| id | value\n")
        # enabled
        if "enabled" in b.keys():
            printf('| *enabled* | %s\n'%(b["enabled"]))
        else:
            printf('| *enabled* | True\n')
        # aps_rate
        if "aps_rate" in b.keys():
            printf('| *aps_rate* | %.1f\n'%(b["aps_rate"]))
        else:
            printf('| *aps_rate* | 0.0\n')
        if "aps_rate_step_change" in b.keys():
            printf('| *aps_rate_step_change* | %.1f\n'%(b["aps_rate_step_change"]))
        if "aps_rate_step_count" in b.keys():
            printf('| *aps_rate_step_count* | %d\n'%(b["aps_rate_step_count"]))

        # num_tasks
        if "num_tasks" in b.keys():
            printf('| *num_tasks* | %d\n'%(b["num_tasks"]))
        # workflow
        if "scenario_workflow" in b.keys():
            workflow_is_defined = b["scenario_workflow"] in jb["workflows"].keys()
            if workflow_is_defined:
                printf('| *scenario_workflow* | <<%s>>\n'%(b["scenario_workflow"]))
            else:
                printf('| *scenario_workflow* | Error, scenario_workflow *%s* is not defined\n'%(b["scenario_workflow"]))
        elif "scenario_actions" in b.keys():
            printf('| *scenario_actions* \n')
            printf('| \n')
            printf('[%header,cols="1a,2a"]\n')
            printf('!===\n')
            printf('! id ! weight \n')
            for a in b["scenario_actions"]:
                printf('! %d\n'%(a["id"]))
                if "weight" in a.keys():
                    printf('! %d \n'%(a["weight"]))
                else:
                    printf('! - \n')
            printf('!===\n')
        # warmup_duration1
        if "warmup_duration1" in b.keys():
            printf('| *warmup_duration1* | %d\n'%(b["warmup_duration1"]))
        else:
            printf('| *warmup_duration1* | 0\n')
        # warmup_duration2
        if "warmup_duration2" in b.keys():
            printf('| *warmup_duration2* | %d\n'%(b["warmup_duration2"]))
        else:
            printf('| *warmup_duration2* | 0\n')
        # benchmark_duration
        if "benchmark_duration" in b.keys():
            printf('| *benchmark_duration* | %d\n'%(b["benchmark_duration"]))
        else:
            printf('| *benchmark_duration* | 0\n')
        # benchmark_iterations
        if "benchmark_iterations" in b.keys():
            printf('| *benchmark_iterations* | %d\n'%(b["benchmark_iterations"]))
        else:
            printf('| *benchmark_iterations* | 0\n')

        printf("|===\n")

    # JSON Config
    printf("\n")
    printf("== JSON Configuration File\n")
    printf("\n")
    printf("[source,json,linenums]\n")
    printf("----\n")
    printf(json.dumps(jb, indent=4))
    printf("\n")
    printf("----\n")

    # JVM System Properties
    printf("\n")
    printf("== JVM System Properties\n")
    printf("\n")
    printf("* java.vendor: %s\n"%(java_json["jvm.system.properties"]["java.vendor"]))
    printf("* java.version: %s\n"%(java_json["jvm.system.properties"]["java.version"]))
    printf("* java.runtime.version: %s\n"%(java_json["jvm.system.properties"]["java.runtime.version"]))
    printf("* os.name: %s\n"%(java_json["jvm.system.properties"]["os.name"]))
    printf("* os.arch: %s\n"%(java_json["jvm.system.properties"]["os.arch"]))

    # JVM Runtime Options
    printf("\n")
    printf("== JVM Runtime Options\n")
    printf("\n")
    idx = 0
    for arg in java_json["jvm.runtime.options"]:
        idx += 1
        printf("* Option %d: %s\n"%(idx, arg))

    report_fh.close()
    convertAdocToHtml(report_fn)
    return

def convertAdocToHtml(adocFilename):
    #print("Converting %s to HTML..."%(adocFilename))
    #print("Current working directory: %s"%(os.getcwd()))
    filename = os.path.join(os.getcwd(), adocFilename)
    #print("Adoc file: %s"%(filename))
    asciidoctor = Asciidoctor.Factory.create()
    stylesheetUrl = "https://raw.githubusercontent.com/tulipltt/Tulip/refs/heads/main/docs/css/adoc-foundation.css"
    attributes = Attributes.builder().attribute("linkcss", False).attribute("data-uri", True).attribute("allow-uri-read", True).attribute("stylesheet", stylesheetUrl).build()
    asciidoctor.requireLibrary("asciidoctor-diagram")
    asciidoctor.convertFile(
        File(filename),
        Options.builder().toFile(True).attributes(attributes).safe(SafeMode.UNSAFE).build()
    )
    asciidoctor.shutdown()

if __name__ == "__main__":
    if len(sys.argv) >= 2:
        filename = sys.argv[1]
        createReport(filename)
