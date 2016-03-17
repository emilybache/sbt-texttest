
organization := "org.texttest"

name := "myapp"

scalaVersion := "2.11.7"

texttestAppNames := List("myapp")
texttestTestPathSelection := Map("myapp" -> "mypath")
texttestBatchSessionName := "all"
texttestRoot := Some(s"${sys.env.getOrElse("TEXTTEST_SANDBOX", "/tmp")}/texttest_home")
