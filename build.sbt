
sbtPlugin := true

organization := "org.texttest"

name := "sbt-texttest"

publishMavenStyle := false

bintrayRepository := "sbt-texttest"

bintrayVcsUrl := Some("http://github.com/emilybache/sbt-texttest")

licenses += ("MIT", url("https://github.com/emilybache/sbt-texttest/blob/master/license.txt"))

sbtrelease.ReleasePlugin.releaseSettings
