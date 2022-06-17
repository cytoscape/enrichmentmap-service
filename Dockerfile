# 1. Build the image:
#
#    docker build -t enrichmentmap-service .
#
# 2. Run the Image:
#
#    docker run --rm -it enrichmentmap-service:latest
#
# ===[ Build Stage ]====================================================================================================
FROM eclipse-temurin:17-jdk-centos7 AS MAVEN_BUILD

# Install wget
RUN yum -y upgrade
RUN yum -y install wget

# Install Maven
ENV MAVEN_VERSION 3.6.3
RUN wget https://downloads.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz
RUN tar -xvf ./apache-maven-$MAVEN_VERSION-bin.tar.gz -C /opt/

ENV M2_HOME /opt/apache-maven-$MAVEN_VERSION
ENV maven.home $M2_HOME
ENV M2 $M2_HOME/bin
ENV PATH $M2:$PATH

# Build the project
WORKDIR /home/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# ===[ Package Stage ]==================================================================================================
FROM eclipse-temurin:17-jre-centos7

ARG APP_VERSION=0.0.1-SNAPSHOT
ARG JAR_FILE=enrichmentmap-service-${APP_VERSION}.jar

COPY --from=MAVEN_BUILD /home/app/target/${JAR_FILE} /usr/local/lib/enrichmentmap-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/enrichmentmap-service.jar"]