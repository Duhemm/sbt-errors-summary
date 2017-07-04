package sbt
package errorssummary

import scala.compat.Platform.EOL
import org.scalatest.{FlatSpec, Matchers}
import xsbti.{Problem, Severity}

class BasicConciseReporterSpec
    extends FlatSpec
    with Matchers
    with CompilerSpec
    with ConciseReporterSpec {
  s"A `ConciseReporter` running $scalaVersion" should "collect errors" in collectMessagesFor(
    "foobar") { (problems, messages) =>
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

  it should "respect colors setting" in {
    val code                = """error"""
    val configWithColors    = ReporterConfig(colors = true, shortenPaths = false)
    val configWithoutColors = configWithColors.withColors(false)
    val expectedText        = "[1] /tmp/src.scala:1:"

    collectMessagesFor(code, configWithColors) { (problems, messages) =>
      problems should have length 1

      messages should have length 2
      val (_, msg) = messages.head
      val lines    = msg.split(EOL)
      lines(0).length should be > expectedText.length
    }

    collectMessagesFor(code, configWithoutColors) { (problems, messages) =>
      problems should have length 1

      messages should have length 2
      val (_, msg) = messages.head
      val lines    = msg.split(EOL)
      lines(0) shouldBe expectedText
    }
  }

  it should "strip prefix if told to" in {
    val code = """error"""
    val configWithFullPaths =
      ReporterConfig(colors = false, shortenPaths = true)
    val expectedText = "[1] src.scala:1:"

    collectMessagesFor(code, configWithFullPaths) { (problems, messages) =>
      problems should have length 1

      messages should have length 2
      val (_, msg) = messages.head
      val lines    = msg.split(EOL)
      lines(0) shouldBe expectedText
    }
  }

  it should "not strip prefix if told not to" in {
    val code = """error"""
    val configWithoutFullPaths =
      ReporterConfig(colors = false, shortenPaths = false)
    val expectedText = "[1] /tmp/src.scala:1:"

    collectMessagesFor(code, configWithoutFullPaths) { (problems, messages) =>
      problems should have length 1

      messages should have length 2
      val (_, msg) = messages.head
      val lines    = msg.split(EOL)
      lines(0) shouldBe expectedText
    }
  }
}
