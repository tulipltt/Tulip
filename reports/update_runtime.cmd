REM python3 jython/jython-cli.py report.py
call jbang run --runtime-option -Dstdout.encoding=UTF-8 --runtime-option -Dpython.console.encoding=UTF-8 python_jvm_tulip.java --keep-java report.py

REM copy package.txt+report_py.java report.txt
REM copy report.txt report_py.java
call jbang run FileModifier.java report_py.java

REM del report.txt

copy report_py.java  ..\tulip-runtime\src\main\java\io\github\tulipltt\tulip\report
