package org.texttest.sbtplugin

import java.io.{IOException, File}
import java.nio.file.{Paths, Files, Path}

import sbt.{Process, Logger}

/**
 * Class that knows how to run texttests
 */
class TexttestRunner(log: Logger) extends TexttestUtil(log) {

  def findTextTestExecutable(maybeTexttestExecutable: Option[String]): Path = {
    maybeTexttestExecutable match {
      case Some(texttestExecutable) => {
        if (new File(texttestExecutable).exists) {
          Paths.get(texttestExecutable)
        }
        else {
          throw new RuntimeException("Unable to run texttest. Parameter 'texttestExecutable' is specified as " + texttestExecutable + " but this file is not found. Please use 'pip install texttest' to install texttest on your system")
        }
      }
      case None => {
        findTextTestOnPath() match {
          case Some(texttestExecutable) =>
            texttestExecutable
          case None =>
            throw new RuntimeException("Unable to run texttest. 'texttestExecutable' parameter is not specified, and 'texttest' was not found on your $PATH. Please use 'pip install texttest' to install texttest on your system")
        }
      }
    }
  }

  private def findTextTestOnPath(): Option[Path] = {
    val maybePath = sys.env.get("PATH")
    maybePath match {
      case Some(path) => {
        val candidates: Array[Option[Path]] = path.split(System.getProperty("path.separator")).map(possibleLocation(_)).filter(_.isDefined)
        if (candidates.nonEmpty) {
          log.info("found texttest on PATH at location: " + candidates.head)
          Some(candidates.head.get)
        } else {
          log.info(s"texttest not found on PATH ${path}")
          None
        }
      }
      case _ => {
        log.info("PATH environment variable was not defined, therefore could not find texttest executable on it.")
        None
      }
    }

  }

  private def possibleLocation(pathElement: String): Option[Path] = {
    val candidate = new File(pathElement).toPath.resolve("texttest")
    if (Files.exists(candidate)) {
      Some(candidate)
    } else {
      None
    }
  }

  def runTexttest(texttestExecutable: Path,
                  texttestTestCaseLocation: Path,
                  texttestRootPath: Path,
                  projectDir: Path,
                  appName: String,
                  batchSessionName: String,
                  sandbox: Path,
                  testFailureIgnore: Boolean,
                  testPathSelection: Option[String],
                  testNameSelection: Option[String]): Unit = {
    var arguments: List[String] = List(
              texttestExecutable.toString,
              "-a", appName,
              "-b", batchSessionName,
              "-c", projectDir.toString,
              "-d", texttestTestCaseLocation + System.getProperty("path.separator") + texttestRootPath.toString)
    arguments = testPathSelection match {
      case Some(pathSelection) => arguments ::: List("-ts", pathSelection)
      case _ => arguments
    }
    arguments = testNameSelection match {
      case Some(nameSelection) => arguments ::: List("-t", nameSelection)
      case _ => arguments
    }
    log.info("Will start texttest with this command: " + arguments)

    try {
      val exitStatus = Process(arguments, projectDir.toFile, "TEXTTEST_TMP" -> sandbox.toString) ! log
      if (exitStatus != 0 && !testFailureIgnore) {
        throw new RuntimeException("There were test failures")
      }
    }
    catch {
      case ioe: IOException => {
        throw new RuntimeException("TextTest failed to execute", ioe)
      }
      case ie: InterruptedException => {
        throw new RuntimeException("TextTest failed to execute", ie)
      }
    }
  }
}
