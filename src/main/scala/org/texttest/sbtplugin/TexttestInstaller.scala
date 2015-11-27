package org.texttest.sbtplugin


import java.io.IOException
import java.nio.file.{Files, Paths, Path}

import sbt.Logger

class TexttestInstaller(log: Logger) {

  def installUnderTexttestRoot(testCaseLocation: Path, appName: String, customRoot: Option[String]) {
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
