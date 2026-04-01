#!/bin/bash
#
# Tulip Report Generator
#
# This script runs the new report generator against an existing benchmark output JSON file.
# It uses a temporary Gradle initialization script to resolve the project's dependencies
# and run the generator with the correct classpath.
#
# Usage: ./report.sh <benchmark_output.json>
#

if [ -z "$1" ]; then
    echo "Usage: ./report.sh <benchmark_output.json>"
    exit 1
fi

REPORT_FILE="$1"

# Ensure we have an absolute path to the report file
if [[ "$REPORT_FILE" != /* ]]; then
    REPORT_FILE="$(pwd)/$REPORT_FILE"
fi

# Check if the report file exists
if [ ! -f "$REPORT_FILE" ]; then
    echo "Error: File not found - $REPORT_FILE"
    exit 1
fi

# Run the task using gradlew
./gradlew -q :tulip-report:run --args="$REPORT_FILE"
EXIT_CODE=$?

exit $EXIT_CODE
