package xsbt

import scala.tools.nsc.Settings

import xsbti.Position
import xsbti.Problem
import xsbti.Reporter
import xsbti.Severity

object WrappedReporter {
  def apply(
      settings: Settings,
      reporter: Reporter,
      posTransform: Position => Position
  ): scala.tools.nsc.reporters.Reporter = {
    val transformedReporter =
      new TransformedPositionsReporter(reporter, posTransform)
    DelegatingReporter(settings, transformedReporter)
  }
}

private class TransformedPositionsReporter(
    reporter: Reporter,
    posTransform: Position => Position
) extends Reporter {
  def comment(pos: Position, msg: String): Unit =
    reporter.comment(posTransform(pos), msg)

  def hasErrors(): Boolean =
    reporter.hasErrors()
  def hasWarnings(): Boolean =
    reporter.hasWarnings()

  def log(problem: Problem): Unit = {
    val newProblem = new Problem {
      override def category(): String   = problem.category()
      override def message(): String    = problem.message()
      override def position(): Position = posTransform(problem.position())
      override def severity(): Severity = problem.severity()
    }
    reporter.log(newProblem)
  }

  def printSummary(): Unit =
    reporter.printSummary()

  def problems(): Array[xsbti.Problem] =
    reporter.problems()

  def reset(): Unit =
    reporter.reset()
}
