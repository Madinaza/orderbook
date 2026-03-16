FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml pom.xml

COPY services/shared-contracts/pom.xml services/shared-contracts/pom.xml
COPY services/auth-service/pom.xml services/auth-service/pom.xml
COPY services/api-gateway/pom.xml services/api-gateway/pom.xml
COPY services/trading-service/pom.xml services/trading-service/pom.xml
COPY services/market-data-service/pom.xml services/market-data-service/pom.xml
COPY services/discovery-service/pom.xml services/discovery-service/pom.xml

COPY services/shared-contracts/src services/shared-contracts/src
COPY services/auth-service/src services/auth-service/src

RUN mvn -pl services/auth-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/services/auth-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app/app.jar"]