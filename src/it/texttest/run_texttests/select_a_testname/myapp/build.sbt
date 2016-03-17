
organization := "org.texttest"

name := "myapp"

scalaVersion := "2.11.7"

texttestRoot := Some(s"${sys.env.getOrElse("TEXTTEST_SANDBOX", "/tmp")}/texttest_home")
texttestTestNameSelection := Map("myapp" -> "goodbye")
