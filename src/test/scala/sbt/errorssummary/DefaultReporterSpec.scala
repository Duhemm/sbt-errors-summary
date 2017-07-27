package sbt
package errorssummary

import scala.compat.Platform.EOL
import org.scalatest.{FlatSpec, Matchers}
import xsbti.{Maybe, Severity}

class BasicReporterSpec
    extends FlatSpec
    with Matchers
    with CompilerSpec
    with ReporterSpec {
  s"A `Reporter` running $scalaVersion" should "collect errors" in collectMessagesFor(
    "foobar") { (problems, messages) =>
    problems should have length 1
    messages should have length 3

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
    messages should have length 5

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
    messages should have length 3

    problems(0).severity shouldBe Severity.Error
    problems(0).position.line.get shouldBe 2
  }

  it should "collect problems when the code doesn't need wrapping" in collectMessagesFor {
    """object Foobar {
      |  def foo(): Int = "hello"
      |}""".stripMargin
  } { (problems, messages) =>
    problems should have length 1
    messages should have length 3

    problems(0).severity shouldBe Severity.Error
    problems(0).position.line.get shouldBe 2
  }

  it should "respect colors setting" in {
    val code                = """error"""
    val configWithColors    = defaultConfig.withColors(true)
    val configWithoutColors = defaultConfig.withColors(false)
    val expectedText        = "[E1] /tmp/src.scala"

    collectMessagesFor(code, configWithColors) { (problems, messages) =>
      problems should have length 1

      messages should have length 3
      val (_, msg) = messages.head
      val lines    = msg.split(EOL)
      lines(0).length should be > expectedText.length
    }

    collectMessagesFor(code, configWithoutColors) { (problems, messages) =>
      problems should have length 1

      messages should have length 3
      val (_, msg) = messages.head
      val lines    = msg.split(EOL)
      lines(0) shouldBe expectedText
    }
  }

  it should "strip prefix if told to" in {
    val code                     = """error"""
    val configWithShortenedPaths = defaultConfig.withShortenPaths(true)
    val expectedText             = "[E1] src.scala"

    collectMessagesFor(code, configWithShortenedPaths) {
      (problems, messages) =>
        problems should have length 1

        messages should have length 3
        val (_, msg) = messages.head
        val lines    = msg.split(EOL)
        lines(0) shouldBe expectedText
    }
  }

  it should "not strip prefix if told not to" in {
    val code                        = """error"""
    val configWithoutShortenedPaths = defaultConfig.withShortenPaths(false)
    val expectedText                = "[E1] /tmp/src.scala"

    collectMessagesFor(code, configWithoutShortenedPaths) {
      (problems, messages) =>
        problems should have length 1

        messages should have length 3
        val (_, msg) = messages.head
        val lines    = msg.split(EOL)
        lines(0) shouldBe expectedText
    }
  }

  it should "not show column numbers when told not to" in {
    val code                       = """error"""
    val configWithoutColumnNumbers = defaultConfig.withColumnNumbers(false)
    val expectedText               = "[E1] /tmp/src.scala"

    collectMessagesFor(code, configWithoutColumnNumbers) {
      (problems, messages) =>
        problems should have length 1

        messages should have length 3
        val (_, msg) = messages.head
        val lines    = msg.split(EOL)
        lines(0) shouldBe expectedText
    }
  }

  it should "show column numbers when told to" in {
    val code                    = """    error""".stripMargin
    val configWithColumnNumbers = defaultConfig.withColumnNumbers(true)
    val expectedText            = "[E1] /tmp/src.scala"

    collectMessagesFor(code, configWithColumnNumbers) { (problems, messages) =>
      problems should have length 1

      messages should have length 3
      val (_, msg) = messages.head
      val lines    = msg.split(EOL)
      lines(0) shouldBe expectedText
    }
  }

  it should "not include problems with unknown positions in summary" in {
    // `Manifest[_].erasure` is deprecated since 2.10
    // We'll just get an un-positioned message about deprecation.
    val code = """implicitly[Manifest[Int]].erasure"""
    collectMessagesFor(code, filePath = Maybe.nothing[String]) {
      (problems, messages) =>
        problems should have length 1
        messages should have length 2
        val (sev, msg) = messages.head
        sev shouldBe Level.Warn

        // Make sure that we don't print a summary at all
        // (no errors have a path that is set)
        msg.split(EOL) should have length 1
    }
  }

  it should "reverse problem order when told to" in {
    val code =
      """    error
        |moreError""".stripMargin
    val configWithReversedOrder = defaultConfig.withReverseOrder(true)
    val expectedText            = "[E2] /tmp/src.scala"

    collectMessagesFor(code, configWithReversedOrder) { (problems, messages) =>
      problems should have length 2

      messages should have length 4
      val (_, msg) = messages.head
      val lines    = msg.split(EOL)
      lines(0) shouldBe expectedText
    }
  }

  it should "not show the legend when told not to" in {
    val code                = "error"
    val configWithoutLegend = defaultConfig.withShowLegend(false)
    val expectedText        = "[E1] /tmp/src.scala"

    collectMessagesFor(code, configWithoutLegend) { (problems, messages) =>
      problems should have length 1

      messages should have length 2
      val (_, msg) = messages.head
      val lines    = msg.split(EOL)
      lines(0) shouldBe expectedText
    }
  }
}
