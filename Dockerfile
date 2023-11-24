FROM amazoncorretto:17-alpine-jdk

ADD target/*.jar app.jar
ADD src/main/resources/log4j2.xml log4j2.xml

ENTRYPOINT ["java","-Dlog4j.configurationFile=log4j2.xml", "-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
