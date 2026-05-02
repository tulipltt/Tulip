REM Use this script to create the ZIP file to be uploaded to Maven Central
REM https://central.sonatype.com/
REM
set version=2.3.1

REM Remove staging folder
rd/q/s .\tulip-runtime\build\staging-deploy

REM Publish JARs to staging folder
call .\gradlew.bat publish

REM Sign the artifacts
pushd .\tulip-runtime\build\staging-deploy\io\github\tulipltt\tulip-runtime\%version%
gpg -ab tulip-runtime-%version%-javadoc.jar
gpg -ab tulip-runtime-%version%-sources.jar
gpg -ab tulip-runtime-%version%.jar
gpg -ab tulip-runtime-%version%-all.jar
gpg -ab tulip-runtime-%version%.module
gpg -ab tulip-runtime-%version%.pom
popd

REM ZIP the files to be uploaded to Maven Central
pushd .\tulip-runtime\build\staging-deploy
zip -r tulip-runtime-%version%.zip io
popd

REM Display artifact files and ZIP file
dir /s .\tulip-runtime\build\staging-deploy\io\github\tulipltt\tulip-runtime\%version%
dir   .\tulip-runtime\build\staging-deploy\*.zip
