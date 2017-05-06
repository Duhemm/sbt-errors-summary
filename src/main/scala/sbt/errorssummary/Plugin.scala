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

  private val reporterSettings =
    compilerReporter in compile := {
      val parent    = (compilerReporter in compile).value
      val logger    = streams.value.log
      val sourceDir = sourceDirectory.value.getAbsolutePath
      Some(new ConciseReporter(logger, sourceDir, parent))
    }
}
