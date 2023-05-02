import Dependencies.{Auth, Routing, Helper}

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

lazy val root = (project in file("."))
  .settings(
    name := "project-mipt"
  )
  .aggregate(
    auth,
    routing,
    helper,
  )
  .dependsOn(
    auth,
    routing,
    helper,
  )

lazy val auth = (project in file("auth"))
  .settings(
    name := "project-auth",
    libraryDependencies ++= Auth.dependencies,
    assembly / assemblyJarName := "auth.jar",
    assembly / mainClass := Some("auth.AuthMain")
  )

lazy val routing = (project in file("routing"))
  .settings(
    name := "project-routing",
    libraryDependencies ++= Routing.dependencies,
    assembly / assemblyJarName := "routing.jar",
    assembly / mainClass := Some("routing.RoutingMain")
  )

lazy val helper = (project in file("helper"))
  .settings(
    name := "project-helper",
    libraryDependencies ++= Helper.dependencies,
  )
