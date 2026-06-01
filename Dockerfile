FROM eclipse-temurin:17-jdk
COPY target/contract-system-1.0.0.jar app.jar
CMD ["java", "-jar", "/app.jar"]
