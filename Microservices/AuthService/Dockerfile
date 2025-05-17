FROM openjdk:25-ea-4-jdk-oraclelinux9

WORKDIR /app

COPY target/*.jar AuthService.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "AuthService.jar"]