package sbt.errorssummary

import xsbti.{Maybe, Position, Severity}
import scala.compat.Platform.EOL

object ScalacFormat extends ReporterFormatFactory {
  override def apply(reporter: ConfigurableReporter): ReporterFormat =
    new ScalacFormat(reporter)
}

/**
 * A format that mimics that of scalac.
 * Adapted from `sbt.LoggerReporter`
 * Copyright 2002-2009 LAMP/EPFL
 * see LICENSE_Scala
 * Original author: Martin Odersky
 */
class ScalacFormat(reporter: ConfigurableReporter)
    extends ReporterFormat(reporter) {

  override def formatProblem(problem: Problem): String =
    format(problem.position, problem.message)

  override def printSummary(): Unit = {
    val warnings = reporter.allProblems.count(_.severity == Severity.Warn)
    if (warnings > 0)
      reporter.logger.warn(
        countElementsAsString(warnings, "warning") + " found")
    val errors = reporter.allProblems.count(_.severity == Severity.Error)
    if (errors > 0)
      reporter.logger.error(countElementsAsString(errors, "error") + " found")
  }

  private def format(pos: Position, msg: String): String = {
    if (pos.sourcePath.isEmpty && pos.line.isEmpty)
      msg
    else {
      val out          = new StringBuilder
      val sourcePrefix = m2o(pos.sourcePath).getOrElse("")
      val columnNumber = m2o(pos.pointer).map(_.toInt + 1).getOrElse(1)
      val lineNumberString = m2o(pos.line)
        .map(":" + _ + ":" + columnNumber + ":")
        .getOrElse(":") + " "
      out.append(sourcePrefix + lineNumberString + msg + EOL)
      val lineContent = pos.lineContent
      if (!lineContent.isEmpty) {
        out.append(lineContent + EOL)
        for (space <- m2o(pos.pointerSpace))
          out.append(space + "^") // pointer to the column position of the error/warning
      }
      out.toString
    }
  }

  private def countElementsAsString(n: Int, elements: String): String = {
    n match {
      case 0 => "no " + elements + "s"
      case 1 => "one " + elements
      case 2 => "two " + elements + "s"
      case 3 => "three " + elements + "s"
      case 4 => "four " + elements + "s"
      case _ => "" + n + " " + elements + "s"
    }
  }

  private def m2o[T](m: Maybe[T]): Option[T] =
    if (m.isDefined) Some(m.get) else None

}
