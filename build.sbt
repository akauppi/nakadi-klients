name := "nakadi-klients"

version := "0.3.1-SNAPSHOT"

organization := "org.zalando.nakadi.client"

scalaVersion := "2.11.7"

// @Benjamin: what is this needed for? AKa050216
//
crossPaths := false

// @Benjamin: the definitions of the flags we can see online. I'd be more interested in seeing the reason why
//        exactly these flags are selected, in the comments. AKa050216
//
// For comparison, my normal set is:
/* scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  //"-Xfatal-warnings",         // enable to treat warnings as errors (note: stops compilation at the first module with a warning)
  //
  "-language", "postfixOps"   // allow postfix operators globally
)
*/
scalacOptions ++= Seq(
  "-deprecation",             // Emit warning and location for usages of deprecated APIs.
  "-feature",                 // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked",               // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings",         // Fail the compilation if there are any warnings.
  "-Xlint",                   // Enable recommended additional warnings.
  "-Ywarn-adapted-args",      // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code",         // Warn when dead code is identified.
  "-Ywarn-inaccessible",      // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override",  // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen",     // Warn when numerics are widened.

  "-language", "postfixOps"   // allow postfix operators globally
)

// Where to get 'scoop'
//
// @Benjamin: I am confused.
//    - https://maven.zalando.net/#nexus-search;quick~scoop has version 0.1.2-SNAPSHOT but the public git repo
//      (pom.xml) is still at 0.1.0-SNAPSHOT: https://github.com/zalando/scoop/blob/master/pom.xml#L7
//    - sbt cannot fetch 'scoop' from maven.zalando.net because there is no .pom exported, 'sbt update' fails with:
//      <<
//        [warn] ==== Zalando Nexus: tried
//        [warn]   https://maven.zalando.net/de/zalando/scoop/scoop/0.1.2-SNAPSHOT/scoop-0.1.2-SNAPSHOT.pom
//      <<
//
//  i.e. how is 'scoop' intended to be delivered to 'nakadi-klients'?
//      one way is to simply tie to public git repo (sbt can do that), bypassing Maven. Should I try that? AKa050216
//
resolvers ++= Seq(
  //"Zalando Nexus" at "https://maven.zalando.net"
  //,
  Resolver.mavenLocal   // enable to use locally pushed Scoop builds
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-experimental" % "2.0.3",
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.7.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.3",
  "com.google.guava" % "guava" % "19.0",
  "de.zalando.scoop" % "scoop" % "0.1.0-SNAPSHOT",

  // test
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