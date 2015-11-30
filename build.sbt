import bintray.Keys._

sbtPlugin := true

organization := "org.texttest"

name := "sbt-texttest"

publishMavenStyle := false

bintrayPublishSettings

repository in bintray := "sbt-plugins"

licenses += ("MIT", url("https://github.com/emilybache/sbt-texttest/blob/master/license.txt"))

bintrayOrganization in bintray := None

sbtrelease.ReleasePlugin.releaseSettings
