package sbt.errorssummary

import scala.compat.Platform.EOL

object ClassicFormat extends ReporterFormatFactory {
  override def apply(reporter: ConfigurableReporter): ClassicFormat =
    new ClassicFormat(reporter)
}

class ClassicFormat(reporter: ConfigurableReporter)
    extends DefaultReporterFormat(reporter) {
  override def formatProblem(problem: Problem): String = {
    val line = toOption(problem.position.line).map(_ + ":").getOrElse("")
    val col = problem.position.lineOffset match {
      case Some(offset) if reporter.config.columnNumbers => (offset + 1) + ":"
      case _                                             => ""
    }

    val sourcePath =
      formatSourcePath(problem).map(
        _ + ":" + colored(colorFor(problem), s"$line$col")
      )

    val text =
      List(sourcePath, formatMessage(problem), formatSource(problem)).flatten
        .mkString(EOL)

    val prefix = s"${extraSpace(problem.severity)}[E${problem.id}] "
    prefixed(reporter.config.errorIdColor, prefix, text)
  }
}
