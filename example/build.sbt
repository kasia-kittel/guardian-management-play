
publishArtifact := false

//TODO fix multiproject build
libraryDependencies ++= Seq(
  "com.gu" %% "management" % "5.35",
  "com.gu" %% "management-internal" % "5.35",
  "com.gu" %% "management-logback" % "5.35",
  "com.typesafe.play" %% "play" % "2.6.1",
  "com.typesafe.play" %% "play-test" % "2.6.1" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % "test",
  guice
)




// needed for Play
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
