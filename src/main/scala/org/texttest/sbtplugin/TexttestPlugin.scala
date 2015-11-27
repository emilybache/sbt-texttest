package org.texttest.sbtplugin

import java.io.File

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

    val texttestGlobalInstall = settingKey[Boolean]("Whether to install this suite of texttests under $TEXTTEST_HOME. Defaults to true.")
    val texttestInstallClasspath = settingKey[Boolean]("Whether to add the project's classpath to the environment used when running your tests. Defaults to true.")
    val texttestExtraSearchDirectory = settingKey[String]("What folder to use for the 'extra_search_directory' setting you may have in your config.app file. This plugin will write an interpreter_options file containing the CLASSPATH to this folder, if you set the property 'texttestInstallClasspath' to true. Defaults to {target.value}/texttest_extra_config")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    texttestInstall := {
      println(s"install! ${target.value}, classpath ${(managedClasspath in Test).value.map(_.data).mkString(File.pathSeparator)}, texttest_home ${sys.env("TEXTTEST_HOME")}")
    },
    texttestRun := {
      println(s"run! ${texttestAppName.value} in ${baseDirectory.value}")
      val exitCode = Process("echo", Seq("hello World!")) ! streams.value.log
    },
    texttestAppName := name.value,
    texttestTestPathSelection := None,
    texttestTestNameSelection := None,
    texttestBatchSessionName := "all",
    texttestTestCaseLocation := s"${baseDirectory.value}/it/texttest",
    texttestExecutable := None,
    texttestGlobalInstall := true,
    texttestInstallClasspath := true,
    texttestExtraSearchDirectory := "${target.value}/texttest_extra_config"
  )


}
