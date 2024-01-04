import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}

val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    organization := "nokdotie",
    name         := "eircode-address-database",
    version := DateTimeFormatter
      .ofPattern("yyyyMMdd.HHmmss.n")
      .withZone(ZoneOffset.UTC)
      .format(Instant.now()),
    scalaVersion := scala3Version,
    resolvers += Resolver.githubPackages("nokdotie"),
    libraryDependencies ++= List(
      "ie.nok"         %% "scala-libraries"      % "20231029.200446.985541447",
      "com.google.maps" % "google-maps-services" % "2.2.0",
      "org.scalameta"  %% "munit"                % "0.7.29" % Test,
      "org.scalameta"  %% "munit-scalacheck"     % "0.7.29" % Test
    ),
    Test / publishArtifact := true
  )
