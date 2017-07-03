package sbt
package errorssummary

import sbt.AutoPlugin
import sbt.Keys.{compile, compilerReporter, sourceDirectory, streams}

object Plugin extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin
  override def trigger  = allRequirements

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(reporterSettings) ++
      inConfig(Test)(reporterSettings)

  private val insideEmacs =
    sys.props.contains("INSIDE_EMACS")

  private val inCI =
    System.console() == null ||
      sys.props.get("CI").exists(_ == "true") ||
      sys.props.get("CONTINUOUS_INTEGRATION").exists(_ == "true") ||
      sys.props.contains("BUILD_NUMBER")

  private val reporterSettings =
    compilerReporter in compile := {
      val parent = (compilerReporter in compile).value
      val logger = streams.value.log

      // Disable color in Emacs and CI
      val enableColors = !(insideEmacs || inCI)

      // We don't shorten paths if we're inside Emacs
      val sourceDir =
        if (insideEmacs) "" else sourceDirectory.value.getAbsolutePath

      val reporter =
        new ConciseReporter(logger, enableColors, sourceDir, parent)
      Some(reporter)
    }
}
