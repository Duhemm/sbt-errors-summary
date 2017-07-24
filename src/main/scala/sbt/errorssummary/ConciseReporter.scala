package sbt
package errorssummary

import xsbti.{Maybe, Position, Reporter, Severity}

import java.io.File
import scala.Console.RESET
import scala.compat.Platform.EOL

/**
 * A concise reporter that shows a summary of the lines with errors and warnings.
 *
 * @param logger The logger that will receive the output of the reporter.
 * @param base   The base prefix to remove from paths.
 * @param parent Another reporter that should also receive the messages.
 */
private class ConciseReporter(logger: Logger,
                              base: String,
                              parent: Option[Reporter],
                              sourcePositionMapper: Position => Position,
                              config: ReporterConfig)
    extends Reporter {

  private val _problems = collection.mutable.ArrayBuffer.empty[Problem]
  private var _nextID   = 1

  override def reset(): Unit = {
    parent.foreach(_.reset())
    _problems.clear()
    _nextID = 1
  }

  override def hasErrors(): Boolean =
    hasErrors(_problems)

  override def hasWarnings(): Boolean =
    hasWarnings(_problems)

  override def printSummary(): Unit = {
    parent.foreach(_.printSummary())

    if (config.reverseOrder) {
      _problems.reverse.foreach(logFull)
    }

    val log: String => Unit =
      (line: String) =>
        if (hasErrors(_problems)) logger.error(line)
        else if (hasWarnings(_problems)) logger.warn(line)
        else logger.info(line)

    _problems
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

    if (config.showLegend && problems().nonEmpty)
      logger.info("Legend: Ln = line n, Cn = column n, En = error n")
  }

  override def problems(): Array[xsbti.Problem] =
    _problems.toArray

  override def log(pos: Position, msg: String, sev: Severity): Unit = {
    parent.foreach(_.log(pos, msg, sev))

    val mappedPos = sourcePositionMapper(pos)
    val problemID = if (pos.sourceFile.isDefined) nextID() else -1
    val problem   = Problem(problemID, sev, msg, mappedPos)
    _problems += problem

    // If we show errors in reverse order, they'll all be shown
    // in `printSummary`.
    if (!config.reverseOrder) {
      logFull(problem)
    }
  }

  override def comment(pos: Position, msg: String): Unit =
    parent.foreach(_.comment(pos, msg))

  /**
   * Log the full error message for `problem`.
   *
   * @param problem The problem to log.
   */
  private def logFull(problem: Problem): Unit = {
    val text = showText(problem)
    problem.severity match {
      case Severity.Error => logger.error(text)
      case Severity.Warn  => logger.warn(text)
      case Severity.Info  => logger.info(text)
    }
  }
  private def hasErrors(problems: Seq[Problem]): Boolean =
    problems.exists(_.severity == Severity.Error)

  private def hasWarnings(problems: Seq[Problem]): Boolean =
    problems.exists(_.severity == Severity.Warn)

  /**
   * Returns the absolute path of `file` with `base` stripped, if the reporter
   * if configured with `shortenPaths = true`.
   *
   * @param file The file whose path to show.
   * @return The absolute path of `file` with `base` stripped if `shortenPaths = true`,
   *         or the original path otherwise.
   */
  private def showPath(file: File): Option[String] = {
    val absolutePath = Option(file).map(_.getAbsolutePath)
    if (config.shortenPaths) absolutePath.map(_.stripPrefix(base))
    else absolutePath
  }

  /**
   * Shows `str` with color `color` if the reporter is configured with
   * `colors = true`.
   *
   * @param color The color to use
   * @param str   The string to color.
   * @return The colored string if `colors = true`, `str` otherwise.
   */
  private def colored(color: String, str: String): String =
    if (config.colors) s"${RESET}${color}${str}${RESET}"
    else str

  /**
   * Put a prefix `prefix` at the beginning of `paragraph`, indents all lines.
   *
   * @param prefix    The prefix to insert.
   * @param paragraph The block of text to prefix and indent.
   * @return The prefixed and indented paragraph.
   */
  private def prefixed(prefix: String, paragraph: String): String =
    augmentString(paragraph).lines
      .mkString(colored(config.errorIdColor, prefix),
                EOL + " " * prefix.length,
                "")

  /**
   * Shows the full error message for `problem`.
   *
   * @param problem The problem to show
   * @return The full error message.
   */
  private def showText(problem: Problem): String = {
    val file   = problem.position.pfile
    val line   = problem.position.pline
    val offset = problem.position.poffset

    val noString = Option.empty[String]
    val (position, lineContent, pointer, prefix) =
      file.fold((noString, noString, noString, "")) { f =>
        val lineCol = {
          val lineText = line.fold("") { l =>
            colored(colorFor(problem), s"L$l:")
          }

          val colText =
            offset.filter(_ => config.columnNumbers).fold("") { c =>
              s"C${c + 1}:"
            }

          s"$lineText$colText"
        }

        val position = colored(config.sourcePathColor, f)
        val lineContent =
          Option(problem.position.lineContent)
            .filter(_.nonEmpty)
            .map(prefixed(lineCol, _))

        val pointer = problem.position.pointerSpace.map { sp =>
          prefixed(lineCol, s"$sp^")
        }

        val prefix = s"${extraSpace(problem.severity)}[E${problem.id}] "

        (Some(position), lineContent, pointer, prefix)
      }

    val text =
      List(position, Some(problem.message), lineContent, pointer).flatten
        .mkString(EOL)

    prefixed(prefix, text)
  }

  private def extraSpace(severity: Severity): String =
    severity match {
      case Severity.Warn => " "
      case Severity.Info => " "
      case _             => ""
    }

  /**
   * Retrieves the right color to use for `problem` based on Severity.
   *
   * @param problem The problem to show.
   * @return The ANSI string to set the right color.
   */
  private def colorFor(problem: Problem): String =
    problem.severity match {
      case Severity.Info  => config.infoColor
      case Severity.Error => config.errorColor
      case Severity.Warn  => config.warningColor
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
        colored(config.errorIdColor, s" [E${problem.id}]")
    }

  private def nextID(): Int = {
    val id = _nextID
    _nextID += 1
    id
  }

  implicit class MyPosition(position: Position) {
    def pfile: Option[String] = position.sourceFile.flatMap(showPath)
    def pline: Option[Int]    = position.line.map(_.toInt)
    def poffset: Option[Int]  = position.offset.map(_.toInt)
  }

  private case class Problem(id: Int,
                             severity: Severity,
                             message: String,
                             rawPosition: Position)
      extends xsbti.Problem {
    override val category: String = ""

    override val position: Position = new Position {
      def offset: Maybe[Integer] =
        pointerSpace
          .map(s => Maybe.just(s.length: Integer))
          .getOrElse(Maybe.nothing[Integer])

      def line(): Maybe[Integer] = rawPosition.line()

      def lineContent(): String = rawPosition.lineContent()

      def pointer(): Maybe[Integer] = rawPosition.pointer()

      def pointerSpace(): Maybe[String] = rawPosition.pointerSpace()

      def sourceFile(): Maybe[File] = rawPosition.sourceFile()

      def sourcePath(): Maybe[String] = rawPosition.sourcePath()
    }
  }

}
