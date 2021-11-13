# annotation
./gradlew clean -p annotation
./gradlew publishReleasePublicationToMavenRepository -p annotation

# compiler
./gradlew clean -p compiler
#./gradlew publishReleasePublicationToMavenLocal -p compiler # for test
./gradlew publishReleasePublicationToMavenRepository -p compiler

# router
./gradlew clean -p router
#./gradlew publishReleasePublicationToMavenLocal -p router # for test
./gradlew publishReleasePublicationToMavenRepository -p router

# gradle-plugin
./gradlew clean -p gradle-plugin
#./gradlew publishReleasePublicationToMavenLocal -p gradle-plugin # for test
./gradlew publishReleasePublicationToMavenRepository -p gradle-plugin