#!/bin/bash
#
# Build and test the tulip-report module, generating coverage reports.
#

# Navigate to project root
cd "$(dirname "$0")/.."

# Run build and jacoco report
./gradlew :tulip-report:build :tulip-report:jacocoTestReport
