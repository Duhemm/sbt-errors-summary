package sbt
package errorssummary

import xsbti.{Maybe, Position, Severity}

import java.io.File
import scala.Console.RESET
import scala.compat.Platform.EOL

/**
 * A flexible reporter whose configuration is provided by a `ReporterConfig`.
 * This configuration indicated whether to use colors, how to format messages,
 * etc.
 *
 * @param logger The logger that will receive the output of the reporter.
 * @param base   The base prefix to remove from paths.
 * @param parent Another reporter that should also receive the messages.
 * @param sourcePositionMapper A function that transforms positions.
 * @param config The configuration for this reporter.
 */
private final class Reporter(val logger: Logger,
                             val base: String,
                             parent: Option[xsbti.Reporter],
                             sourcePositionMapper: Position => Position,
                             val config: ReporterConfig)
    extends xsbti.Reporter
    with ConfigurableReporter {

  private val format    = config.format(this)
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

    format.printSummary()

  }

  override def allProblems: Seq[Problem] =
    _problems.toSeq

  override def problems(): Array[xsbti.Problem] =
    _problems.toArray

  override def log(pos: Position, msg: String, sev: Severity): Unit = {
    parent.foreach(_.log(pos, msg, sev))

    val mappedPos = sourcePositionMapper(pos)
    val problemID = if (pos.sourceFile.isDefined) nextID() else -1
    val problem   = Problem(problemID, sev, msg, mappedPos, "")
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
    val text = format.formatProblem(problem)
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

  private def nextID(): Int = {
    val id = _nextID
    _nextID += 1
    id
  }

}
