#!/bin/bash
./gradlew -q --stop
rm -f -r ./bin ./build

./gradlew -q clean

param1="$1"
if [[ "$param1" == "" ]]; then
   ./gradlew -q :tulip-main:run
else
   ./gradlew -q :tulip-main:run "$param1"
fi

./gradlew -q --stop
