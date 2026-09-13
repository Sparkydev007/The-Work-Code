FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY shared/common-lib/pom.xml shared/common-lib/pom.xml
COPY services/api-gateway/pom.xml services/api-gateway/pom.xml
RUN mvn -q -pl services/api-gateway -am dependency:go-offline -DskipTests || true
COPY shared shared
COPY services/api-gateway/src services/api-gateway/src
RUN mvn -q -pl services/api-gateway -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S workcode && adduser -S workcode -G workcode
USER workcode
COPY --from=build /build/services/api-gateway/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
