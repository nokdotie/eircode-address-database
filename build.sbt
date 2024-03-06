import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}

val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    organization := "ie.nok",
    name         := "eircode-address-database",
    version := DateTimeFormatter
      .ofPattern("yyyyMMdd.HHmmss.n")
      .withZone(ZoneOffset.UTC)
      .format(Instant.now()),
    scalaVersion     := scala3Version,
    githubOwner      := "nokdotie",
    githubRepository := "eircode-address-database",
    resolvers += Resolver.githubPackages("nokdotie"),
    libraryDependencies ++= List(
      "ie.nok"         %% "scala-libraries"      % "20240306.143044.567021287" % "compile->compile;test->test",
      "com.google.maps" % "google-maps-services" % "2.2.0",
      "org.scalameta"  %% "munit"                % "0.7.29"                    % Test,
      "org.scalameta"  %% "munit-scalacheck"     % "0.7.29"                    % Test
    ),
    Test / publishArtifact := true
  )
