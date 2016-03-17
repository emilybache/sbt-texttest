
lazy val root = project.in( file(".") ).dependsOn( plugin )
lazy val plugin = uri(s"file:///${sys.env("SOURCES_ROOT")}/sbt-texttest")

//addSbtPlugin("org.texttest" % "sbt-texttest" % "1.0-SNAPSHOT")

