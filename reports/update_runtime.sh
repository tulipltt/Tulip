jbang run --runtime-option -Dstdout.encoding=UTF-8 --runtime-option -Dpython.console.encoding=UTF-8 python_jvm_tulip.java --keep-java report.py

jbang run FileModifier.java report_py.java

cp report_py.java  ../tulip-runtime/src/main/java/io/github/tulipltt/tulip/report/
