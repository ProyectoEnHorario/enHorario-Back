# Build stage
FROM maven:3.9-eclipse-temurin-17 as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV DATABASE_URL=jdbc:postgresql://postgres:5432/enhorario \
    DB_USERNAME=postgres \
    DB_PASSWORD=postgres \
    PORT=8080 \
    SERVER_PORT=8080 \
    JWT_SECRET=your-secret-key-change-in-production

ENTRYPOINT ["java", "-jar", "app.jar"]
