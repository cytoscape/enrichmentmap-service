# 1. Build the image:
#
#    docker build -t enrichmentmap-service .
#
# 2. Run the Image:
#
#    docker run --rm -p 8080:8080 -it enrichmentmap-service:latest
#
# ===[ Build Stage ]====================================================================================================
FROM maven:3.8.6-eclipse-temurin-17 AS MAVEN_BUILD

# Build the project
WORKDIR /home/app

COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# ===[ Package Stage ]==================================================================================================
FROM openjdk:17.0.2-jdk-slim

ARG APP_VERSION=1.0.0
ARG JAR_FILE=enrichmentmap-service-${APP_VERSION}.jar

COPY --from=MAVEN_BUILD /home/app/target/${JAR_FILE} /usr/local/lib/enrichmentmap-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/enrichmentmap-service.jar"]