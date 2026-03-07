#!/bin/bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env install

# Clean
mkdir -p ~/.m2
find ~/.m2 -name 'tulip-runtime*.jar' -print | sort
rm -f -r ~/.m2/repository/io/github/tulipltt/tulip/tulip-runtime

# ---------------------------------------------------------------

# reports
pushd ./reports
./update_runtime.sh
rm report_py.java
popd

# spotless
./gradlew spotlessApply
# ./gradlew fixJbangMarker

echo ""
echo "Waiting for 5 seconds..."
for i in {5..1}; do
    echo -n "$i "
    sleep 1
done
echo "done."

# Publish tulip-runtime.jar to local Maven
./gradlew clean
./gradlew :tulip-runtime:build
./gradlew :tulip-runtime:publishToMavenLocal

find ~/.m2 -name 'tulip-runtime*.jar' -print | sort

date

# ---------------------------------------------------------------
