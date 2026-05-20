# Stage 1: Build
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Stage 2: Run
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
