FROM sbtscala/scala-sbt:graalvm-ce-22.3.0-b2-java17_1.8.2_2.13.10 as build
#FROM sbtscala/scala-sbt:eclipse-temurin-focal-17.0.5_8_1.8.2_2.13.10 as build
COPY . /root/
WORKDIR /root
RUN sbt assembly

FROM eclipse-temurin:17-jre-focal
RUN mkdir -p /opt/app
COPY --from=build /root/target/*.jar /opt/app/app.jar
COPY --from=build /root/auth/target/scala-2.13/project-auth-assembly-0.1.0-SNAPSHOT.jar /opt/app/auth.jar
COPY --from=build /root/routing/target/scala-2.13/project-routing-assembly-0.1.0-SNAPSHOT.jar /opt/app/routing.jar
COPY --from=build /root/helper/target/scala-2.13/project-helper-assembly-0.1.0-SNAPSHOT.jar /opt/app/helper.jar

