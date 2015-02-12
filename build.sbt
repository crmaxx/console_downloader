organization := "ru.gravenet"

name := "console-downloader"

version := "0.1"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "org.rogach" %% "scallop" % "0.9.5",
  "pl.project13.scala" %% "rainbow" % "0.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9"
)

assemblyJarName in assembly := s"console-downloader-${version.value}.jar"
