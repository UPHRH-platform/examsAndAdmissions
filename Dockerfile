FROM openjdk:17-oracle
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY src/main/resources/keys/U2JTvZDDV8xo7fk_4wuc-d5Rf64OLmhziQEHcGnUshM /opt/U2JTvZDDV8xo7fk_4wuc-d5Rf64OLmhziQEHcGnUshM
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app.jar"]
