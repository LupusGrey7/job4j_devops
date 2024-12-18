FROM gradle:8.11.1-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
