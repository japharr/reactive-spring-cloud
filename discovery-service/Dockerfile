FROM maven:3.6.3-jdk-11 as builder
WORKDIR /app
COPY ./pom.xml ./
RUN mvn compile
COPY . .
RUN mvn package

FROM openjdk:12-alpine
VOLUME /tmp
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]