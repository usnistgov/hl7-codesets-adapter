FROM openjdk:21
RUN mkdir -p /usr/local/codesets-adapter
COPY codesets-adapter.jar /usr/local/codesets-adapter
CMD ["java", "-Xmx900m", "-Xss256k", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/usr/local/codesets-adapter/codesets-adapter.jar"]