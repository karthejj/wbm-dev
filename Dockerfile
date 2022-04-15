FROM openjdk:8
EXPOSE 8389
ADD target/wbmdemo-docker.jar wbmdemo-docker.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/wbmdemo-docker.jar"]