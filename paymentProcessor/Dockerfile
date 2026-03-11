FROM amazoncorretto:17-alpine AS builder

WORKDIR /app

COPY pom.xml mvnw* ./
COPY .mvn .mvn

RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM amazoncorretto:17-alpine

WORKDIR /app

COPY --from=builder /app/target/paymentProcessor-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
