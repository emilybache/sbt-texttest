package org.texttest.sbtplugin

import java.io.{IOException, File}
import java.nio.file.{Files, Path, Paths}

import sbt._
import sbt.Keys._

/**
 * Sbt plugin that can install and execute texttests as part of an sbt build.
 */
object TexttestPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val texttestInstall = taskKey[Unit]("install texttests under TEXTTEST_HOME, and create classpath file needed for tests to run.")
    val texttestRun = taskKey[Unit]("run texttests found under it/texttest")

    val texttestAppName = settingKey[String]("texttest application name, sent to texttest with '-a' flag. Defaults to {name.value}.")
    val texttestTestPathSelection = settingKey[Option[String]]("texttest path selection, sent to texttest with '-ts' flag. Optional.")
    val texttestTestNameSelection = settingKey[Option[String]]("texttest test name selection, sent to texttest with '-t' flag. Optional.")
    val texttestBatchSessionName = settingKey[String]("texttest batch session name, sent to texttest with '-b' flag. Defaults to 'all'")

    val texttestTestCaseLocation = settingKey[String]("Where the texttest test cases are located in this repo. Defaults to {baseDirectory.value}/it/texttest")
    val texttestExecutable = settingKey[Option[String]]("Full path to the texttest executable. If it is on your $PATH then you do not need to set this.")
    val texttestRoot = settingKey[Option[String]]("The path to TEXTTEST_ROOT - ie where the texttest runner will find your test cases. If the parameter 'texttestGlobalInstall' is set to true, then this must be set, and is expected to be a global location on your machine where you may have many test suites installed. If you don't set this value, we use the environment variable $TEXTTEST_ROOT If that is not set, we will use the environment variable $TEXTTEST_HOME as a fallback option. If neither are set, we use the value of 'texttestTestCaseLocation' as a last resort.")

    val texttestGlobalInstall = settingKey[Boolean]("Whether to install this suite of texttests under $TEXTTEST_HOME. Defaults to true.")
    val texttestInstallClasspath = settingKey[Boolean]("Whether to add the project's classpath to the environment used when running your tests. Defaults to true.")
    val texttestExtraSearchDirectory = settingKey[String]("What folder to use for the 'extra_search_directory' setting you may have in your config.app file. This plugin will write an interpreter_options file containing the CLASSPATH to this folder, if you set the property 'texttestInstallClasspath' to true. Defaults to {target.value}/texttest_extra_config")
    val texttestFailureIgnore = settingKey[Boolean]("If tests fail and this flag is set to true, we exit with code zero anyway. This is useful if you need to clean up resources before exiting the build. Defaults to false")
    val texttestSandbox = settingKey[String]("Where texttest should put test result files and files relating to test runs. Defaults to target/sandbox")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    texttestAppName := name.value,
    texttestTestPathSelection := None,
    texttestTestNameSelection := None,
    texttestBatchSessionName := "all",
    texttestTestCaseLocation := s"${baseDirectory.value}/src/it/texttest",
    texttestExecutable := None,
    texttestGlobalInstall := true,
    texttestInstallClasspath := true,
    texttestExtraSearchDirectory := s"${target.value}/texttest_extra_config",
    texttestRoot := None,
    texttestFailureIgnore := false,
    texttestSandbox := s"${target.value}/sandbox",
    texttestInstall := {
      val installer = new TexttestInstaller(streams.value.log)
      if (texttestGlobalInstall.value) {
        val texttestRootPath = installer.findTexttestRootPath(texttestRoot.value, Paths.get(texttestTestCaseLocation.value))
        installer.installUnderTexttestRoot(Paths.get(texttestTestCaseLocation.value), texttestAppName.value, texttestRootPath)
      }
      if (texttestInstallClasspath.value) {
        val classpath = (fullClasspath in Test).value.map(_.data).toList
        installer.writeClasspathToInterpreterOptionsFile(texttestAppName.value, Paths.get(texttestExtraSearchDirectory.value), classpath)
      }

    },
    texttestRun := {
      texttestInstall.value // install task is needed before the run task will be able to work
      streams.value.log.info(s"running texttest application ${texttestAppName.value} in ${baseDirectory.value}")
      val runner = new TexttestRunner(streams.value.log)
      val texttest = runner.findTextTestExecutable(texttestExecutable.value)
      val texttestRootPath = runner.findTexttestRootPath(texttestRoot.value, Paths.get(texttestTestCaseLocation.value))
      runner.runTexttest(texttest,
        Paths.get(texttestTestCaseLocation.value),
        texttestRootPath,
        baseDirectory.value.toPath,
        texttestAppName.value,
        texttestBatchSessionName.value,
        Paths.get(texttestSandbox.value),
        texttestFailureIgnore.value,
        texttestTestPathSelection.value,
        texttestTestNameSelection.value)
    }
  )


}
