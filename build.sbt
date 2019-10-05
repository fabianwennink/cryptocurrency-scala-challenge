name := "learning-scala"
version := "0.1"
scalaVersion := "2.13.0"

// Dependencies
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.9",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "com.typesafe.akka" %% "akka-remote" % "2.5.23",
  "io.spray" %%  "spray-json" % "1.3.4"
)
