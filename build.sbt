name := "nakadi-klients"

version := "0.3.0-SNAPSHOT"

organization := "org.zalando.nakadi.client"

scalaVersion := "2.11.7"

crossPaths := false

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)

// Needed to get 'scoop' from local Maven repo ('~/.m2/repository/...')
//
resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "com.google.guava" % "guava" % "19.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.7.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.3",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.0.2",
  "de.zalando.scoop" % "scoop" % "0.1.0-SNAPSHOT",

  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "io.undertow" % "undertow-core"    % "1.2.12.Final" % "test",
  "io.undertow" % "undertow-servlet" % "1.2.12.Final" % "test",
  "org.apache.commons" % "commons-io" % "1.3.2" % "test",
  "com.google.code.findbugs" % "jsr305" % "1.3.9" % "test",

  // docker-it-scala
  // See -> http://finelydistributed.io/integration-tests-with-docker/
  //
  "com.whisk" %% "docker-testkit-scalatest" % "0.5.4" % "test"  //,
  //"com.whisk" %% "docker-testkit-config" % "0.5.4" % "test"
)

// see http://www.scala-sbt.org/0.13/docs/Publishing.html
publishTo := {
  val nexus = "https://maven.zalando.net/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "content/repositories/releases")
}