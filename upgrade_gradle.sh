# ./gradlew wrapper --gradle-version latest
#
# https://github.com/JetBrains/amper/blob/release/0.4/docs/Usage.md
#
# https://github.com/JetBrains/amper/blob/release/0.4/syncVersions.sh
#
# https://github.com/JetBrains/amper/releases
#
# https://gradle.org/releases/
#
# https://github.com/Kotlin/kotlinx.serialization - 1.7.2 requries Kotlin 2.0.20
#
GRADLE_VERSION=9.5.0

./gradlew --no-daemon wrapper --gradle-version $GRADLE_VERSION && ./gradlew wrapper
