FROM openjdk:11
COPY target/scala-3.0.2/mychat-assembly-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]