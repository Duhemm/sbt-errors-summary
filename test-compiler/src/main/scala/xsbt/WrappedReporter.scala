package xsbt

import scala.tools.nsc.Settings
import xsbti.{Position, Reporter, Severity}

object WrappedReporter {
  def apply(settings: Settings,
            reporter: Reporter,
            posTransform: Position => Position)
    : scala.tools.nsc.reporters.Reporter = {
    val transformedReporter =
      new TransformedPositionsReporter(reporter, posTransform)
    DelegatingReporter(settings, transformedReporter)
  }
}

private class TransformedPositionsReporter(reporter: Reporter,
                                           posTransform: Position => Position)
    extends Reporter {
  def comment(pos: Position, msg: String): Unit =
    reporter.comment(posTransform(pos), msg)

  def hasErrors(): Boolean =
    reporter.hasErrors()
  def hasWarnings(): Boolean =
    reporter.hasWarnings()

  def log(pos: Position, msg: String, severity: Severity): Unit =
    reporter.log(posTransform(pos), msg, severity)

  def printSummary(): Unit =
    reporter.printSummary()

  def problems(): Array[xsbti.Problem] =
    reporter.problems()

  def reset(): Unit =
    reporter.reset()
}
