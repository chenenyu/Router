# annotation
./gradlew clean publish -p annotation
./gradlew bintrayUpload -p annotation

# compiler
./gradlew clean publish -p compiler
./gradlew bintrayUpload -p compiler

# router
./gradlew clean publish -p router
./gradlew bintrayUpload -p router

# gradle-plugin
./gradlew clean publish -p gradle-plugin
./gradlew bintrayUpload -p gradle-plugin