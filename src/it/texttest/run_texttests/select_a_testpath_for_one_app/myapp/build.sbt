
organization := "org.texttest"

name := "myapp"

scalaVersion := "2.11.7"

texttestAppNames := List("myapp", "anotherapp")
texttestRoot := Some(s"${sys.env.getOrElse("TEXTTEST_SANDBOX", "/tmp")}/texttest_home")
texttestTestPathSelection := Map("myapp" -> "goodbye")
