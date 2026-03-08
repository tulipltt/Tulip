REM -------------------------------------------------------------

md %USERPROFILE%\.m2

dir /s %USERPROFILE%\.m2\tulip-runtime*.jar

rd/q/s %USERPROFILE%\.m2\repository\io\github\tulipltt\tulip-runtime

REM -------------------------------------------------------------

pushd .\reports
call .\update_runtime.cmd
del report_py.java
popd

call .\gradlew spotlessApply
REM call .\gradlew fixJbangMarker
timeout /t 5

call .\gradlew :tulip-runtime:build
call .\gradlew :tulip-runtime:publishToMavenLocal
dir /s %USERPROFILE%\.m2\tulip-runtime*.jar
REM echo.
REM echo "jbang io.github.tulipltt:tulip-runtime:<version>"

REM -------------------------------------------------------------
