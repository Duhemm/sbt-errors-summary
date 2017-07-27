package sbt.errorssummary

import xsbti.{Position, Severity}
import scala.compat.Platform.EOL

import java.util.Optional

/**
 * Helper object for easy configuration.
 */
object DefaultReporterFormat extends ReporterFormatFactory {
  override def apply(reporter: ConfigurableReporter): DefaultReporterFormat =
    new DefaultReporterFormat(reporter)
}

/**
 * Default format for reporter.
 *
 * @param reporter The reporter that uses this format.
 */
class DefaultReporterFormat(reporter: ConfigurableReporter)
    extends ReporterFormat(reporter) {

  override def formatProblem(problem: Problem): String = {
    val file   = problem.position.pfile
    val line   = problem.position.pline
    val offset = problem.position.poffset

    val noString = Option.empty[String]
    val (position, lineContent, pointer, prefix) =
      file.fold((noString, noString, noString, "")) { f =>
        val position = colored(reporter.config.sourcePathColor, f)
        val lineContent =
          Option(problem.position.lineContent)
            .filter(_.nonEmpty)
        val pointer = toOption(problem.position.pointerSpace).map { sp =>
          s"$sp^"
        }
        val prefix = s"${extraSpace(problem.severity)}[E${problem.id}] "

        (Some(position), lineContent, pointer, prefix)
      }

    val lineCol = {
      val lineText = line.fold("") { l =>
        s"L$l:"
      }
      val colText =
        offset.filter(_ => reporter.config.columnNumbers).fold("") { c =>
          s"C${c + 1}:"
        }

      s"$lineText$colText"
    }

    val errorPosition = prefixed(
      colorFor(problem),
      lineCol,
      List(lineContent, pointer).flatten.mkString(EOL))
    val text =
      List(position, Some(problem.message), Some(errorPosition)).flatten
        .mkString(EOL)

    prefixed(reporter.config.errorIdColor, prefix, text)
  }

  override def printSummary(): Unit = {
    val log: String => Unit =
      (line: String) =>
        if (reporter.hasErrors) reporter.logger.error(line)
        else if (reporter.hasWarnings) reporter.logger.warn(line)
        else reporter.logger.info(line)

    reporter.allProblems
      .groupBy(_.position.pfile)
      .foreach {
        case (None, _) =>
          ()
        case (Some(file), inFile) =>
          val sorted =
            inFile
              .sortBy(_.position.pline)
              .map(showProblemLine)
              .flatten

          if (sorted.nonEmpty) {
            val line = s"""$file: ${sorted.mkString(", ")}"""
            log(line)
          }
      }

    if (reporter.config.showLegend && reporter.allProblems.nonEmpty)
      reporter.logger.info("Legend: Ln = line n, Cn = column n, En = error n")
  }

  /**
   * Returns spaces to fix alignment given the `severity`.
   */
  private def extraSpace(severity: Severity): String =
    severity match {
      case Severity.Warn => " "
      case Severity.Info => " "
      case _             => ""
    }

  /**
   * Shows the line at which `problem` occured and the id of the problem.
   *
   * @param problem The problem to show
   * @return A formatted string that shows the line of the problem and its id.
   */
  private def showProblemLine(problem: Problem): Option[String] =
    problem.position.pline.map { line =>
      val color = colorFor(problem)

      colored(color, "L" + line) +
        colored(reporter.config.errorIdColor, s" [E${problem.id}]")
    }

  private implicit class MyPosition(position: Position) {
    def pfile: Option[String] = toOption(position.sourceFile).flatMap(showPath)
    def pline: Option[Int]    = toOption(position.line).map(_.toInt)
    def poffset: Option[Int]  = toOption(position.offset).map(_.toInt)
  }

  private def toOption[T](m: Optional[T]): Option[T] =
    if (m.isPresent) Some(m.get) else None
}
