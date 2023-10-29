import Dependencies.{Auth, Routing, Helper, Images, Utils}

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "project-mipt"
  )
  .aggregate(
    auth,
    routing,
    helper,
    images,
    utils
  )
  .dependsOn(
    auth,
    routing,
    helper,
    images,
    utils
  )

lazy val auth = (project in file("auth"))
  .dependsOn(utils % "test->test")
  .settings(
    name := "project-auth",
    libraryDependencies ++= Auth.dependencies
  )

lazy val routing = (project in file("routing"))
  .settings(
    name := "project-routing",
    libraryDependencies ++= Routing.dependencies
  )

lazy val helper = (project in file("helper"))
  .settings(
    name := "project-helper",
    libraryDependencies ++= Helper.dependencies
  )

lazy val images = (project in file("images"))
  .dependsOn(utils % "test->test")
  .settings(
    name := "project-images",
    libraryDependencies ++= Images.dependencies
  )

lazy val utils = (project in file("utils"))
  .settings(
    name := "utils",
    libraryDependencies ++= Utils.dependencies
  )
