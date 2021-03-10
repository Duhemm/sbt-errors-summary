package sbt
package errorssummary

import sbt.AutoPlugin
import sbt.internal.inc.JavaInterfaceUtil._
import sbt.Keys.{compile, compilerReporter, fileConverter, maxErrors, printWarnings, reportAbsolutePath, sourcePositionMappers, streams}
import xsbti.{FileConverter, Position, Severity, VirtualFileRef}

import java.io.File
import java.nio.file.{ Path => NioPath }
import java.util.Optional
import scala.util.control.NonFatal

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

  def toAbsoluteSource(fc: FileConverter)(pos: Position): Position = {
    val newPath: Option[NioPath] = pos
      .sourcePath()
      .asScala
      .flatMap { path =>
        try {
          Some(fc.toPath(VirtualFileRef.of(path)))
        } catch {
          // catch all to trap wierd path injected by compiler, users, or plugins
          case NonFatal(_) => None
        }
      }

    newPath
      .map { path =>
        new Position {
          override def line(): Optional[Integer] = pos.line()

          override def lineContent(): String = pos.lineContent()

          override def offset(): Optional[Integer] = pos.offset()

          override def pointer(): Optional[Integer] = pos.pointer()

          override def pointerSpace(): Optional[String] = pos.pointerSpace()

          override def sourcePath(): Optional[String] = java.util.Optional.of(path.toAbsolutePath.toString)

          override def sourceFile(): Optional[java.io.File] =
            (try {
              Some(path.toFile.getAbsoluteFile)
            } catch {
              case NonFatal(_) => None
            }).toOptional

          override def startOffset(): Optional[Integer] = pos.startOffset()

          override def endOffset(): Optional[Integer] = pos.endOffset()

          override def startLine(): Optional[Integer] = pos.startLine()

          override def startColumn(): Optional[Integer] = pos.startColumn()

          override def endLine(): Optional[Integer] = pos.endLine()

          override def endColumn(): Optional[Integer] = pos.endColumn()
        }
      }
      .getOrElse(pos)
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
        i.getReportedProblems ++ i.getUnreportedProblems)
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
