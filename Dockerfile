FROM maven:3.9.9-sapmachine-21 AS build
WORKDIR /opt/app
COPY mvnw pom.xml ./
COPY ./src ./src
RUN mvn clean package -DskipTests

LABEL version="0.1"
LABEL authors="kyut"

FROM amazoncorretto:21-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
EXPOSE 8080

WORKDIR /opt/app
COPY --from=build /opt/app/target/*.jar /opt/app/*.jar
ENTRYPOINT ["java", "-jar", "/opt/app/*.jar"]