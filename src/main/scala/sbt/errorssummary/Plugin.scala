package sbt
package errorssummary

import sbt.AutoPlugin
import sbt.Keys.{compile, compilerReporter, sourceDirectory, streams}

import java.io.File

object Plugin extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin
  override def trigger  = allRequirements

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(reporterSettings) ++
      inConfig(Test)(reporterSettings)

  private val insideEmacs =
    sys.env.contains("INSIDE_EMACS")

  private val inCI =
    System.console() == null ||
      sys.env.get("CI").exists(_ == "true") ||
      sys.env.get("CONTINUOUS_INTEGRATION").exists(_ == "true") ||
      sys.env.contains("BUILD_NUMBER")

  private val reporterSettings =
    compilerReporter in compile := {
      val parent = (compilerReporter in compile).value
      val logger = streams.value.log

      // Disable color in Emacs and CI
      val enableColors = !(insideEmacs || inCI)

      // We don't shorten paths if we're inside Emacs
      val sourceDir =
        if (insideEmacs) "" else sys.props("user.dir") + File.separator

      val reporter =
        new ConciseReporter(logger, enableColors, sourceDir, parent)
      Some(reporter)
    }
}
