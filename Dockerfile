FROM gradle:7-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:17
EXPOSE 8080:8080
EXPOSE 9229:9229
EXPOSE 9230:9230
EXPOSE 80:80
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/cbconnecit.portfolio-backend.jar
ENTRYPOINT ["java","-jar","/app/cbconnecit.portfolio-backend.jar"]