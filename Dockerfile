# syntax=docker/dockerfile:1
ARG MODULE
FROM maven:3.9.11-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /workspace
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY services/edge-service/pom.xml services/edge-service/pom.xml
COPY services/order-service/pom.xml services/order-service/pom.xml
COPY services/inventory-service/pom.xml services/inventory-service/pom.xml
COPY services/payment-worker/pom.xml services/payment-worker/pom.xml
COPY services/payment-provider-simulator/pom.xml services/payment-provider-simulator/pom.xml
RUN mvn -q -DskipTests dependency:go-offline || true
COPY common common
COPY services services
RUN mvn -B -pl services/${MODULE} -am -DskipTests package

FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app
COPY --from=build /workspace/services/${MODULE}/target/${MODULE}-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-XX:+ExitOnOutOfMemoryError","-jar","/app/app.jar"]
