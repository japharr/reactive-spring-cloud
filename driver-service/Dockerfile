FROM openjdk:12-alpine
VOLUME /tmp
COPY ./target/*.jar app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]