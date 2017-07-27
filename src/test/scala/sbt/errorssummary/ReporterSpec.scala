package sbt
package errorssummary

import xsbti.Maybe

trait ReporterSpec { self: CompilerSpec =>

  val defaultConfig: ReporterConfig =
    ReporterConfig(colors = false, shortenPaths = false, columnNumbers = false)

  def collectMessagesFor[T](code: String,
                            config: ReporterConfig = defaultConfig,
                            filePath: Maybe[String] =
                              Maybe.just("/tmp/src.scala"),
                            base: String = "/tmp/")(
      fn: (Array[xsbti.Problem], Seq[(Level.Value, String)]) => T): T = {
    val logger   = new RecordingLogger
    val reporter = new Reporter(logger, base, None, identity, config)
    compile(reporter, code, Seq.empty, filePath)
    reporter.printSummary()
    fn(reporter.problems, logger.getAll())
  }

}
