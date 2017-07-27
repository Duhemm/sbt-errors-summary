package sbt
package errorssummary

import sbt.AutoPlugin
import sbt.Keys.{
  compile,
  compilerReporter,
  maxErrors,
  printWarnings,
  sourceDirectory,
  sourcePositionMappers,
  streams
}
import xsbti.Severity

import scala.Console._
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
    reporterConfig := ReporterConfig()
  )

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(reporterSettings) ++
      inConfig(Test)(reporterSettings)

  private val reporterSettings = Seq(
    compilerReporter in compile := {
      val logger  = streams.value.log
      val baseDir = sys.props("user.dir") + File.separator
      val parent  = (compilerReporter in compile).value
      val spms    = Compiler.foldMappers(sourcePositionMappers.value)
      val config  = (reporterConfig in compile).value

      val reporter =
        new Reporter(logger, baseDir, parent, spms, config)
      Some(reporter)
    },
    printWarnings := {
      val maybeReporter = (compilerReporter in compile).value
      val analysis      = compile.value
      val problems = analysis.infos.allInfos.values.flatMap(i =>
        i.reportedProblems ++ i.unreportedProblems)
      val maximumErrors = maxErrors.value
      var errorCount    = 0
      for {
        reporter <- maybeReporter;
        p        <- problems
        if p.severity != Severity.Error || errorCount < maximumErrors
      } {
        if (p.severity == Severity.Error) errorCount += 1
        reporter.log(p.position, p.message, p.severity)
      }

      maybeReporter.foreach(_.printSummary())
    }
  )
}
