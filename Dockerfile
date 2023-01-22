FROM openjdk:8
COPY target/wbmdemo-0.0.1.jar wbmdemo-0.0.1.jar
ENTRYPOINT ["java", "-jar", "/wbmdemo-0.0.1.jar"]