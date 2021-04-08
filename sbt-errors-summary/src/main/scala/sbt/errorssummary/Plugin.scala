package sbt
package errorssummary

import java.io.File

import sbt.Keys.compile
import sbt.Keys.compilerReporter
import sbt.Keys.maxErrors
import sbt.Keys.printWarnings
import sbt.Keys.sourcePositionMappers
import sbt.Keys.streams
import xsbti.Position
import xsbti.Severity

object Plugin extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin
  override def trigger  = allRequirements

  object autoImport {
    val reporterConfig: SettingKey[ReporterConfig] =
      settingKey[ReporterConfig]("Configuration of the error reporter")
  }
  import autoImport._

  override def globalSettings: Seq[Setting[_]] =
    Seq(
      reporterConfig := ReporterConfig()
    )

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(reporterSettings) ++
      inConfig(Test)(reporterSettings)

  // Method copied (with a small adjustments) from https://github.com/sbt/sbt/blob/571005efa0f1b572dae9a3ebc3f4d1bd1c3a86e7/main/src/main/scala/sbt/Defaults.scala
  private def foldMappers(mappers: Seq[Position => Option[Position]]) = {
    mappers.foldRight({ p: Position =>
      p // Fallback if sourcePositionMappers is empty
    }) {
      (mapper, previousPosition) =>
        { p: Position =>
          // To each mapper we pass the position with the absolute source (only if reportAbsolutePath = true of course)
          mapper(p).getOrElse(previousPosition(p))
        }
    }
  }

  private val reporterSettings = Seq(
    compilerReporter in compile := {
      val logger     = streams.value.log
      val baseDir    = sys.props("user.dir") + File.separator
      val spms       = foldMappers(sourcePositionMappers.value)
      val baseConfig = (reporterConfig in compile).value

      // When run in intellij, Emacs or when `sbti.errorssummary.full.paths = true`,
      // don't shorten paths.
      val forceFullPaths =
        sys.props.contains("idea.runid") ||
          sys.env.contains("INSIDE_EMACS") ||
          sys.props.getOrElse("sbt.errorssummary.full.paths", "") == "true"

      val config =
        baseConfig.withShortenPaths(baseConfig.shortenPaths && !forceFullPaths)

      val reporter =
        new Reporter(logger, baseDir, spms, config)
      reporter
    },
    printWarnings := {
      val reporter = (compilerReporter in compile).value
      val analysis = compile.value match {
        case a: sbt.internal.inc.Analysis => a
      }
      val problems = analysis.infos.allInfos.values.flatMap(i =>
        i.getReportedProblems ++ i.getUnreportedProblems
      )
      val maximumErrors = maxErrors.value
      var errorCount    = 0

      for {
        p <- problems
        if p.severity != Severity.Error || errorCount < maximumErrors
      } {
        if (p.severity == Severity.Error) errorCount += 1
        reporter.log(p)
      }

      reporter.printSummary()
    }
  )
}
