organization := "ru.gravenet"

name := "console-downloader"

version := "0.1"

scalaVersion := "2.11.5"

libraryDependencies += "org.rogach" %% "scallop" % "0.9.5"

assemblyJarName in assembly := s"console-downloader-${version.value}.jar"
