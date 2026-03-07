#python3 jython/jython-cli.py report.py
jbang run --runtime-option -Dstdout.encoding=UTF-8 --runtime-option -Dpython.console.encoding=UTF-8 python_jvm_tulip.java --keep-java report.py

# cat package.txt report_py.java > report.txt
# cp   report.txt report_py.java
jbang run FileModifier.java report_py.java

# rm report.txt

cp report_py.java  ../tulip-runtime/src/main/java/io/github/tulipltt/tulip/report/
