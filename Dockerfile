FROM openjdk:8-jdk-alpine
MAINTAINER Erik Levi <levi.erik@gmail.com>
ADD target/DataEnricher-0.0.1-SNAPSHOT.jar data-enricher.jar
ENTRYPOINT ["java", "-jar", "/data-enricher.jar"]