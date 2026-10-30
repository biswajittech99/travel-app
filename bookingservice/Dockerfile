# =========================
# Stage 1: Build
# =========================
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# Copy the complete Maven multi-module project
COPY . .

# Build common-dto and bookingservice
RUN mvn -pl bookingservice -am clean package -Dmaven.test.skip=true


# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the bookingservice executable JAR
COPY --from=build /workspace/bookingservice/target/bookingservice-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]