FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew buildFatJar --no-daemon

FROM eclipse-temurin:25-jre
EXPOSE 8080
RUN mkdir /app

COPY --from=build /app/build/libs/*-all.jar /app/ktor-app.jar

ENTRYPOINT ["java", "-jar", "/app/ktor-app.jar"]