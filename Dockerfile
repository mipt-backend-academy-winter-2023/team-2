FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 as builder

COPY . /app/
WORKDIR /app/

RUN sbt clean
RUN sbt assembly

FROM eclipse-temurin:17-jre-focal
RUN mkdir -p /opt/service
COPY --from=builder /app/auth/target/scala-2.13/auth.jar /opt/service/auth.jar
COPY --from=builder /app/routing/target/scala-2.13/routing.jar /opt/service/routing.jar
