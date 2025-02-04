FROM openjdk:21
RUN mkdir -p /usr/local/codeset-adapter
COPY codeset-adapter.jar /usr/local/codeset-adapter
CMD ["java", "-Xmx900m", "-Xss256k", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/usr/local/codeset-adapter/codeset-adapter.jar"]