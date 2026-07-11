call jbang run -R=-Dstdout.encoding=UTF-8 -R=-Dpython.console.encoding=UTF-8 python_jvm_tulip.java --keep-java report.py

call jbang run FileModifier.java report_py.java

copy report_py.java  ..\tulip-runtime\src\main\java\io\github\tulipltt\tulip\report
