#! /bin/bash




./gradlew -b ./eureka/build.gradle  bootrun &
./gradlew -b ./authorization/build.gradle  bootrun &
./gradlew -b ./gateway/build.gradle  bootrun &
./gradlew -b ./generator/build.gradle  bootrun &