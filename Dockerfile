FROM hseeberger/scala-sbt:graalvm-ce-21.3.0-java17_1.6.2_2.13.8 as build
COPY . /root/
WORKDIR /root
RUN sbt assembly

FROM eclipse-temurin:17-jre-focal
RUN mkdir -p /opt/app
COPY --from=build /root/target/*.jar /opt/app/app.jar

#EXPOSE 7777
#EXPOSE 8081
#EXPOSE 8082
#ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]

