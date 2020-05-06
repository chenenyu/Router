# annotation
#./gradlew clean pBPTML -p annotation
#./gradlew bintrayUpload -p annotation

# compiler
./gradlew clean pBPTML -p compiler
./gradlew bintrayUpload -p compiler

# router
./gradlew clean pBPTML -p router
./gradlew bintrayUpload -p router

# gradle-plugin
./gradlew clean pBPTML -p gradle-plugin
./gradlew bintrayUpload -p gradle-plugin