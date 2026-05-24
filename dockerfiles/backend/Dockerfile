FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace/backend

COPY backend/gradlew ./gradlew
COPY backend/gradle ./gradle
COPY backend/settings.gradle ./settings.gradle
COPY backend/build.gradle ./build.gradle

RUN chmod +x ./gradlew

COPY backend/src ./src
RUN ./gradlew bootJar -x test

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /workspace/backend/build/libs/*.jar /app/app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
