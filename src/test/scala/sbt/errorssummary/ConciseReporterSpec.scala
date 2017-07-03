package sbt
package errorssummary

import xsbti.Problem

trait ConciseReporterSpec { self: CompilerSpec =>

  def collectMessagesFor[T](code: String)(
      fn: (Array[Problem], Seq[(Level.Value, String)]) => T): T = {
    val logger   = new RecordingLogger
    val reporter = new ConciseReporter(logger, "", None)
    compile(reporter, code)
    reporter.printSummary()
    fn(reporter.problems, logger.getAll())
  }

}
