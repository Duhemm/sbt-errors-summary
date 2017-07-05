package sbt
package errorssummary

import xsbti.Problem

trait ConciseReporterSpec { self: CompilerSpec =>

  val defaultConfig: ReporterConfig =
    ReporterConfig(colors = false, shortenPaths = false, columnNumbers = false)

  def collectMessagesFor[T](code: String,
                            config: ReporterConfig = defaultConfig,
                            filePath: String = "/tmp/src.scala",
                            base: String = "/tmp/")(
      fn: (Array[Problem], Seq[(Level.Value, String)]) => T): T = {
    val logger   = new RecordingLogger
    val reporter = new ConciseReporter(logger, base, None, identity, config)
    compile(reporter, code, Seq.empty, filePath)
    reporter.printSummary()
    fn(reporter.problems, logger.getAll())
  }

}
