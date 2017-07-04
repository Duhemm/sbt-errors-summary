package sbt
package errorssummary

import sbt.AutoPlugin
import sbt.Keys.{compile, compilerReporter, sourceDirectory, streams}

import java.io.File

object Plugin extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin
  override def trigger  = allRequirements

  object autoImport {
    val reporterConfig: SettingKey[ReporterConfig] =
      settingKey[ReporterConfig]("Configuration of the error reporter")
  }
  import autoImport._

  override def globalSettings: Seq[Setting[_]] = Seq(
    reporterConfig := ReporterConfig(colors = true, shortenPaths = false)
  )

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(reporterSettings) ++
      inConfig(Test)(reporterSettings)

  private val reporterSettings = Seq(
    compilerReporter in compile := {
      val logger  = streams.value.log
      val baseDir = sys.props("user.dir")
      val parent  = (compilerReporter in compile).value
      val config  = (reporterConfig in compile).value

      val reporter =
        new ConciseReporter(logger, baseDir, parent, config)
      Some(reporter)
    }
  )
}
