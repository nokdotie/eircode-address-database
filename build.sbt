import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}

val scala3Version = "3.3.0"

lazy val root = project
  .in(file("."))
  .settings(
    organization := "ie.nok",
    name := "eircode-address-database",
    version := DateTimeFormatter
      .ofPattern("yyyyMMdd.HHmmss.n")
      .withZone(ZoneOffset.UTC)
      .format(Instant.now()),
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    githubOwner := "nok-ie",
    githubRepository := "eircode-address-database",
    Test / publishArtifact := true
  )
