# FROM openjdk:21

# RUN mkdir -p /usr/local/codesets-adapter

# COPY ./codesets-adapter.jar /usr/local/codesets-adapter
# COPY run.sh /usr/local/bin/run-server.sh
# RUN chmod a+rx /usr/local/bin/run-server.sh

# CMD /usr/local/bin/run-server.sh

# Step 1: Build the JAR file using Maven
FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app
COPY ../../../codeset-adapter/pom.xml .
COPY ../../../codeset-adapter/src ./src

COPY ../../../infrastructure/containers/apps/codesets-adapter/lib/vocab-1.0.jar /app/vocab-1.0.jar
# Manually install the custom dependency into the local Maven repository
RUN mvn install:install-file -Dfile=/app/vocab-1.0.jar -DgroupId=gov.cdc -DartifactId=vocab -Dversion=1.0 -Dpackaging=jar

RUN mvn clean package -DskipTests

# Step 2: Create a lightweight image with only the JAR file
FROM openjdk:7u221-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar /app/codesets-adapter.jar  
ENTRYPOINT ["java", "-jar", "/app/codesets-adapter.jar"]



