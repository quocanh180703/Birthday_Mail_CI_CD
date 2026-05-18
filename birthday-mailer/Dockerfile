FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace/app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/app/target/birthday-mailer-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
