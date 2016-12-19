package org.texttest.sbtplugin

import java.io.{File, IOException}
import java.nio.file.{Files, Path, Paths}

import org.apache.commons.io.FileUtils
import sbt._
import sbt.Keys._

/**
 * Sbt plugin that can install and execute texttests as part of an sbt build.
 */
object TexttestPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val unzipDependencyJars = taskKey[Unit]("take dependency jar files found from 'texttestDependencies' and unzip them under the texttestExtraConfigDir. This is useful if you have config files or data in dependent projects, that you wish to import in this project")
    val texttestInstall = taskKey[Unit]("install texttests under TEXTTEST_HOME, and create classpath file needed for tests to run.")
    val texttestRun = taskKey[Unit]("run texttests found under src/it/texttest")

    val texttestAppNames = settingKey[List[String]]("texttest application names, sent to texttest with '-a' flag. Defaults to List({name.value}).")
    val texttestTestPathSelection = settingKey[Map[String, String]]("texttest path selection, sent to texttest with '-ts' flag. Optional.")
    val texttestTestNameSelection = settingKey[Map[String, String]]("texttest test name selection, sent to texttest with '-t' flag. Optional.")
    val texttestBatchSessionName = settingKey[String]("texttest batch session name, sent to texttest with '-b' flag. Defaults to 'all'")

    val texttestTestCaseLocation = settingKey[File]("Where the texttest test cases are located in this repo. Defaults to {baseDirectory.value}/src/it/texttest")
    val texttestExecutable = settingKey[Option[String]]("Full path to the texttest executable. If it is on your $PATH then you do not need to set this.")
    val texttestRoot = settingKey[Option[String]]("The path to TEXTTEST_ROOT - ie where the texttest runner will find your test cases. If you set this to 'None' it will use the environment variables 'TEXTTEST_ROOT' or 'TEXTTEST_HOME' instead. It defaults to {baseDirectory.value}/src/it/texttest:{target.value}/texttest_extra_config")

    val texttestGlobalInstall = settingKey[Boolean]("Whether to install this suite of texttests under $TEXTTEST_HOME. Defaults to false. If you set this to true, you must make sure texttestRoot is set correctly")
    val texttestInstallClasspath = settingKey[Boolean]("Whether to add the project's classpath to the environment used when running your tests. Defaults to true.")
    val texttestExtraSearchDirectory = settingKey[File]("What folder to use for the 'extra_search_directory' setting you may have in your config.app file. This plugin will write an interpreter_options file containing the CLASSPATH to this folder, if you set the property 'texttestInstallClasspath' to true. Defaults to {target.value}/texttest_extra_config")
    val texttestFailureIgnore = settingKey[Boolean]("If tests fail and this flag is set to true, we exit with code zero anyway. This is useful if you need to clean up resources before exiting the build. Defaults to false")
    val texttestSandbox = settingKey[File]("Where texttest should put test result files and files relating to test runs. Defaults to {target.value}/sandbox")
    val texttestDependencies = settingKey[List[String]]("Which dependencies to unzip their jar files under texttestExtraSearchDirectory so that texttest can access them")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    texttestAppNames := List(name.value),
    texttestTestPathSelection := Map(),
    texttestTestNameSelection := Map(),
    texttestBatchSessionName := "all",
    texttestTestCaseLocation := baseDirectory.value / "src" / "it" / "texttest",
    texttestExecutable := None,
    texttestGlobalInstall := false,
    texttestInstallClasspath := true,
    texttestExtraSearchDirectory := target.value / "texttest_extra_config",
    texttestDependencies := List(),
    // Texttest finds config both under src/it/texttest and also under the extra search dir
    texttestRoot := {
      val texttestsDir = texttestTestCaseLocation.value.absolutePath
      val extraConfigDir = texttestExtraSearchDirectory.value.absolutePath
      Some(s"${texttestsDir}:${extraConfigDir}")
    },
    texttestFailureIgnore := false,
    texttestSandbox := target.value / "sandbox",
    unzipDependencyJars := {
      val log = streams.value.log
      texttestDependencies.value.foreach { (dependencyName: String) =>
        val extraConfigTarget = texttestExtraSearchDirectory.value / dependencyName
        if (extraConfigTarget.exists()) {
          FileUtils.deleteDirectory(extraConfigTarget)
        }
        val configJar: File = (update in Compile).value
          .select(configurationFilter("compile"))
          .filter(_.name.contains(dependencyName))
          .head
        log.info(s"jar: ${configJar} being unzipped to ${extraConfigTarget}")
        IO.unzip(configJar, extraConfigTarget)
      }
    },
    texttestInstall := {
      val log = streams.value.log
      val installer = new TexttestInstaller(log)
      if (texttestGlobalInstall.value) {
        val texttestRootPath = installer.findTexttestRootPath(texttestRoot.value, texttestTestCaseLocation.value.toPath)
        installer.installUnderTexttestRoot(texttestTestCaseLocation.value.toPath, texttestAppNames.value, texttestRootPath)
      }
      if (texttestInstallClasspath.value) {
        val classpath = (fullClasspath in Test).value.map(_.data).toList
        installer.writeClasspathToInterpreterOptionsFile(texttestAppNames.value, texttestExtraSearchDirectory.value.toPath, classpath)
      }

    },
    texttestRun := {
      texttestInstall.value // install task is needed before the run task will be able to work
      val log = streams.value.log
      log.info(s"running texttest applications ${texttestAppNames.value} in ${baseDirectory.value}")
      val runner = new TexttestRunner(log)
      val texttest = runner.findTextTestExecutable(texttestExecutable.value)
      val texttestRootPath = runner.findTexttestRootPath(texttestRoot.value, texttestTestCaseLocation.value.toPath)
      runner.runTexttest(texttest,
        texttestTestCaseLocation.value.toPath,
        texttestRootPath,
        baseDirectory.value.toPath,
        texttestAppNames.value,
        texttestBatchSessionName.value,
        texttestSandbox.value.toPath,
        texttestFailureIgnore.value,
        texttestTestPathSelection.value,
        texttestTestNameSelection.value)
    },
    texttestInstall <<= texttestInstall.dependsOn(unzipDependencyJars),
    texttestRun <<= texttestRun.dependsOn(texttestInstall)
  )


}
