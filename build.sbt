import java.util.jar._

import sbt.Keys.publish

organization in ThisBuild := "com.gu"

releaseSettings

scalaVersion in ThisBuild := "2.11.7"

crossScalaVersions := Seq("2.11.7")

publishArtifact := false

packageOptions in ThisBuild <+= (version, name) map { (v, n) =>
  Package.ManifestAttributes(
    Attributes.Name.IMPLEMENTATION_VERSION -> v,
    Attributes.Name.IMPLEMENTATION_TITLE -> n,
    Attributes.Name.IMPLEMENTATION_VENDOR -> "guardian.co.uk"
  )
}

//TODO organize multiproject

publishTo in ThisBuild <<= (version) { version: String =>
    val publishType = if (version.endsWith("SNAPSHOT")) "snapshots" else "releases"
    Some(
        Resolver.file(
            "guardian github " + publishType,
            file(System.getProperty("user.home") + "/guardian.github.com/maven/repo-" + publishType)
        )
    )
}


scalacOptions in ThisBuild += "-deprecation"

lazy val guardianResolver = resolvers += "Guardian Github" at "http://guardian.github.com/maven/repo-releases"

//common dependencies
libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.0.1"
libraryDependencies += "com.typesafe.play" %% "play-ws-standalone-json" % "1.0.1"
libraryDependencies += "com.typesafe.play" %% "play-ws-standalone-xml" % "1.0.1"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test

libraryDependencies += guice


def managementProject(name: String) = Project(name, file(name)).settings(Seq(
  javacOptions := Seq(
    "-g",
    "-encoding", "utf8"
  ),
  scalacOptions := Seq("-unchecked", "-optimise", "-deprecation",
    "-Xcheckinit", "-encoding", "utf8", "-feature", "-Yinline-warnings",
    "-Xfatal-warnings"
  )
):_*)



lazy val root = Project("management-root", file(".")).enablePlugins(PlayScala).aggregate(
  managementPlay,
  examplePlay)
  .dependsOn(managementPlay,examplePlay)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val managementPlay = managementProject("management-play")
  .settings(guardianResolver)
  .settings(libraryDependencies ++= Seq(
    //ws,
    // see http://code.google.com/p/guava-libraries/issues/detail?id=1095
    "com.google.code.findbugs" % "jsr305" % "1.3.9"
  )
)

lazy val examplePlay = Project(
  "example",
  file("example"))
  .enablePlugins(PlayScala)
  .dependsOn(managementPlay)
  .settings(
    guardianResolver,
    publish := {},
    publishLocal := {}
  )

run in Compile <<= (run in Compile in examplePlay)