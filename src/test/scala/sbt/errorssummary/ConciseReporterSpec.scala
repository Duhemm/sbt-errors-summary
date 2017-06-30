package sbt
package errorssummary

import xsbti.{Problem, Severity}
import compiler.Compiler
import org.scalatest.{FlatSpec, Matchers}

class ConciseReporterSpec extends FlatSpec with Matchers {

  it should "collect errors" in collectMessagesFor("foobar") {
    (problems, messages) =>
      problems should have length 1
      messages should have length 2

      val problem = problems(0)

      problem.severity shouldBe Severity.Error
      problem.position.line.get shouldBe 1
  }

  it should "collect errors in multi line snippets" in collectMessagesFor {
    """foobar
      |   moreError
      |
      |     eventMore""".stripMargin
  } { (problems, messages) =>
    problems should have length 3
    messages should have length 4

    problems(0).severity shouldBe Severity.Error
    problems(0).position.line.get shouldBe 1

    problems(1).severity shouldBe Severity.Error
    problems(1).position.line.get shouldBe 2

    problems(2).severity shouldBe Severity.Error
    problems(2).position.line.get shouldBe 4
  }

  it should "collect problems when imports are present" in collectMessagesFor {
    """import scala.collection.mutable
      |     error""".stripMargin
  } { (problems, messages) =>
    problems should have length 1
    messages should have length 2

    problems(0).severity shouldBe Severity.Error
    problems(0).position.line.get shouldBe 2
  }

  it should "collect problems when the code doesn't need wrapping" in collectMessagesFor {
    """object Foobar {
      |  def foo(): Int = "hello"
      |}""".stripMargin
  } { (problems, messages) =>
    problems should have length 1
    messages should have length 2

    problems(0).severity shouldBe Severity.Error
    problems(0).position.line.get shouldBe 2
  }

  private def collectMessagesFor[T](code: String)(
      fn: (Array[Problem], Seq[(Level.Value, String)]) => T): T = {
    val logger   = new RecordingLogger
    val reporter = new ConciseReporter(logger, "", None)
    val compiler = new Compiler(reporter)
    compiler.compile(code)
    reporter.printSummary()
    fn(reporter.problems, logger.getAll())
  }

}
