package sbt.errorssummary

import java.util.Optional

import sbt.util.Level

trait ReporterSpec { self: CompilerSpec =>

  val defaultConfig: ReporterConfig =
    ReporterConfig(colors = false, shortenPaths = false, columnNumbers = false)

  def collectMessagesFor[T](code: String,
                            config: ReporterConfig = defaultConfig,
                            filePath: Optional[String] =
                              Optional.of("/tmp/src.scala"),
                            base: String = "/tmp/")(
      fn: (Array[xsbti.Problem], Seq[(Level.Value, String)]) => T): T = {
    val logger   = new RecordingLogger
    val reporter = new Reporter(logger, base, identity, config)
    compile(reporter, code, Seq.empty, filePath)
    reporter.printSummary()
    fn(reporter.problems, logger.getAll())
  }

}
