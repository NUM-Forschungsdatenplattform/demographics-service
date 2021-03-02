# We use non-exploded Jar, because exploding Jar causes different classpath order
# and with current application that would cause issues. Once that is fixed, we could
# simply remove this file and use spring-boot Docker plugin to build more optimized image
FROM adoptopenjdk/openjdk11:alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
