FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y curl zip unzip

#https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html
RUN curl -s "https://get.sdkman.io" | bash

ARG sdkman="source ~/.sdkman/bin/sdkman-init.sh"
ARG sdk="$sdkman && sdk"
ARG sbt="$sdkman && sbt"

RUN bash -c "$sdk install java 20.0.2-tem"

RUN bash -c "$sdk install sbt"

WORKDIR /workspace
ADD ./build.sbt .
ADD . .

RUN bash -c "$sbt package"

#ports
EXPOSE 7777
EXPOSE 8081
EXPOSE 8082
EXPOSE 8083

ENTRYPOINT bash -c "$sbt run"
