# Use official Java 17 image as base
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/grpc-demo-1.0-SNAPSHOT.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
