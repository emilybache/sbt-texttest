
organization := "org.texttest"

name := "myapp"

scalaVersion := "2.11.7"

texttestAppName := "myapp"
texttestTestPathSelection := Some("mypath")
texttestBatchSessionName := "all"
texttestRoot := Some(s"${sys.env.getOrElse("TEXTTEST_SANDBOX", "/tmp")}/texttest_home")
