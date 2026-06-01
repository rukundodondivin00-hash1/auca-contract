FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install -DskipTests

FROM eclipse-temurin:17-jdk
COPY --from=build /app/target/contract-system-1.0.0.jar app.jar
CMD ["java", "-jar", "/app.jar"]
