package org.texttest.sbtplugin


import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, Path}

import sbt.Logger

class TexttestInstaller(log: Logger) {

  def installUnderTexttestRoot(testCaseLocation: Path, appName: String, customRoot: Option[String]): Unit = {
    val texttestRoot = findTexttestRootPath(customRoot, testCaseLocation)
    println(s"Will install TextTests globally with name ${appName} under TEXTTEST_ROOT ${texttestRoot}")

    try {
      if (!Files.exists(texttestRoot)) {
        log.warn("TEXTTEST_ROOT did not exist, creating " + texttestRoot)
        Files.createDirectories(texttestRoot)
      }
      val theAppUnderTextTestHome: Path = texttestRoot.resolve(appName)
      if (Files.isSymbolicLink(theAppUnderTextTestHome)) {
        Files.delete(theAppUnderTextTestHome)
      }
      Files.createSymbolicLink(theAppUnderTextTestHome, testCaseLocation)
    }
    catch {
      case ioe: IOException => {
        throw new RuntimeException("unable to install texttests for app " + appName + " under TEXTTEST_ROOT " + texttestRoot, ioe)
      }
      case uoe: UnsupportedOperationException => {
        throw new RuntimeException("unable to install texttests for app " + appName + " under TEXTTEST_ROOT " + texttestRoot, uoe)
      }
    }
  }

  def writeClasspathToEnvironmentFile(appName: String, extraSearchDirectory: Path, classpath: String): Unit = {
    val text = s"-cp ${classpath}"
    try {
      if (!Files.exists(extraSearchDirectory)) {
        Files.createDirectories(extraSearchDirectory)
      }
      val classpathFile: Path = extraSearchDirectory.resolve(s"interpreter_options.${appName}")
      Files.write(classpathFile, text.getBytes(StandardCharsets.UTF_8))
    }
    catch {
      case e: IOException => {
        throw new RuntimeException("Unable to write configuration file for texttest containing the classpath", e)
      }
    }
  }

  private def findTexttestRootPath(customRoot: Option[String], testCaseLocation: Path): Path = {
    val defaultsInOrder: List[Option[String]] = List(customRoot, sys.env.get("TEXTTEST_ROOT"), sys.env.get("TEXTTEST_HOME"))
    val validOptions = defaultsInOrder.filter(_.isDefined)
    if (validOptions.nonEmpty) {
      log.info("choosing texttestRoot: " + validOptions.head)
      Paths.get(validOptions.head.get)
    } else {
      testCaseLocation
    }
  }

}
