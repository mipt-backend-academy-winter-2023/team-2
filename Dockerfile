#FROM hseeberger/scala-sbt
#RUN mkdir -p /exampleapp/out
#WORKDIR 
#COPY . /exampleapp
#FROM sbtscala/scala-sbt:graalvm-ce-22.3.0-b2-java17_1.8.2_3.2.2

FROM hseeberger/scala-sbt:graalvm-ce-21.3.0-java17_1.6.2_2.13.8
COPY . /root/
WORKDIR /root
RUN sbt assembly

FROM eclipse-temurin:17-jre-focal
RUN mkdir -p /opt/app
COPY --from=build /root/target/*.jar /opt/app/app.jar
ENTRYPOINT ["java"]

