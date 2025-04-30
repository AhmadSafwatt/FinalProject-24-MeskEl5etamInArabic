FROM openjdk:25-ea-4-jdk-oraclelinux9

WORKDIR /app

COPY target/*.jar API-Gateway-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "API-Gateway-0.0.1-SNAPSHOT.jar"]