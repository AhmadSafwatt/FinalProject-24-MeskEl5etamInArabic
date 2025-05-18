FROM openjdk:25-ea-4-jdk-oraclelinux9

WORKDIR /app

COPY target/*.jar OrderService.jar

ENTRYPOINT ["java", "-jar","OrderService.jar"]