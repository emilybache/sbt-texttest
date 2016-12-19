
organization := "org.texttest"

name := "myapp"

scalaVersion := "2.11.7"

libraryDependencies += "org.texttest" % "dependency" % "1.0-SNAPSHOT"

texttestAppNames := List("myapp")

texttestDependencies := List("dependency")
