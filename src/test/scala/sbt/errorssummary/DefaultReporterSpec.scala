package sbt
package errorssummary

import scala.compat.Platform.EOL
import org.scalatest.{FlatSpec, Matchers}
import xsbti.{Maybe, Severity}

class DefaultReporterSpec
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

    messages.map(_._2).mkString(EOL) shouldBe
      """[E1] /tmp/src.scala
        |     not found: value foobar
        |     L1: foobar
        |         ^
        |/tmp/src.scala: L1 [E1]
        |Legend: Ln = line n, Cn = column n, En = error n""".stripMargin
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

    messages.map(_._2).mkString(EOL) shouldBe
      """[E1] /tmp/src.scala
        |     not found: value foobar
        |     L1: foobar
        |         ^
        |[E2] /tmp/src.scala
        |     not found: value moreError
        |     L2:    moreError
        |            ^
        |[E3] /tmp/src.scala
        |     not found: value eventMore
        |     L4:      eventMore
        |              ^
        |/tmp/src.scala: L1 [E1], L2 [E2], L4 [E3]
        |Legend: Ln = line n, Cn = column n, En = error n""".stripMargin
  }

  it should "collect problems when imports are present" in collectMessagesFor {
    """import scala.collection.mutable
      |     anError""".stripMargin
  } { (problems, messages) =>
    problems should have length 1
    messages should have length 3

    problems(0).severity shouldBe Severity.Error
    problems(0).position.line.get shouldBe 2

    messages.map(_._2).mkString(EOL) shouldBe
      """[E1] /tmp/src.scala
        |     not found: value anError
        |     L2:      anError
        |              ^
        |/tmp/src.scala: L2 [E1]
        |Legend: Ln = line n, Cn = column n, En = error n""".stripMargin
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
    messages.map(_._2).mkString(EOL) shouldBe
      """[E1] /tmp/src.scala
        |     type mismatch;
        |      found   : String("hello")
        |      required: Int
        |     L2:   def foo(): Int = "hello"
        |                            ^
        |/tmp/src.scala: L2 [E1]
        |Legend: Ln = line n, Cn = column n, En = error n""".stripMargin
  }

  it should "respect colors setting" in {
    val code                = """anError"""
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
    val code                     = """anError"""
    val configWithShortenedPaths = defaultConfig.withShortenPaths(true)
    val expectedText             = "[E1] src.scala"

    collectMessagesFor(code, configWithShortenedPaths) {
      (problems, messages) =>
        problems should have length 1

        messages should have length 3
        val (_, msg) = messages.head
        val lines    = msg.split(EOL)
        lines(0) shouldBe expectedText

        messages.map(_._2).mkString(EOL) shouldBe
          """[E1] src.scala
            |     not found: value anError
            |     L1: anError
            |         ^
            |src.scala: L1 [E1]
            |Legend: Ln = line n, Cn = column n, En = error n""".stripMargin
    }
  }

  it should "not strip prefix if told not to" in {
    val code                        = """anError"""
    val configWithoutShortenedPaths = defaultConfig.withShortenPaths(false)
    val expectedText                = "[E1] /tmp/src.scala"

    collectMessagesFor(code, configWithoutShortenedPaths) {
      (problems, messages) =>
        problems should have length 1

        messages should have length 3
        val (_, msg) = messages.head
        val lines    = msg.split(EOL)
        lines(0) shouldBe expectedText

        messages.map(_._2).mkString(EOL) shouldBe
          """[E1] /tmp/src.scala
            |     not found: value anError
            |     L1: anError
            |         ^
            |/tmp/src.scala: L1 [E1]
            |Legend: Ln = line n, Cn = column n, En = error n""".stripMargin
    }
  }

  it should "not show column numbers when told not to" in {
    val code                       = """anError"""
    val configWithoutColumnNumbers = defaultConfig.withColumnNumbers(false)
    val expectedText               = "[E1] /tmp/src.scala"

    collectMessagesFor(code, configWithoutColumnNumbers) {
      (problems, messages) =>
        problems should have length 1

        messages should have length 3
        val (_, msg) = messages.head
        val lines    = msg.split(EOL)
        lines(0) shouldBe expectedText

        messages.map(_._2).mkString(EOL) shouldBe
          """[E1] /tmp/src.scala
            |     not found: value anError
            |     L1: anError
            |         ^
            |/tmp/src.scala: L1 [E1]
            |Legend: Ln = line n, Cn = column n, En = error n""".stripMargin
    }
  }

  it should "show column numbers when told to" in {
    val code                    = """    anError""".stripMargin
    val configWithColumnNumbers = defaultConfig.withColumnNumbers(true)
    val expectedText            = "[E1] /tmp/src.scala"

    collectMessagesFor(code, configWithColumnNumbers) { (problems, messages) =>
      problems should have length 1

      messages should have length 3
      val (_, msg) = messages.head
      val lines    = msg.split(EOL)
      lines(0) shouldBe expectedText

      messages.map(_._2).mkString(EOL) shouldBe
        """[E1] /tmp/src.scala
          |     not found: value anError
          |     L1C5:     anError
          |               ^
          |/tmp/src.scala: L1 [E1]
          |Legend: Ln = line n, Cn = column n, En = error n""".stripMargin
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

        val deprecationMessage =
          if (scalaVersion.startsWith("2.10"))
            "there were 1 deprecation warning(s); re-run with -deprecation for details"
          else if (scalaVersion.startsWith("2.11"))
            "there was one deprecation warning; re-run with -deprecation for details"
          else
            "there was one deprecation warning (since 2.10.0); re-run with -deprecation for details"

        messages.map(_._2).mkString(EOL) shouldBe
          s""" [E-1] $deprecationMessage
             |Legend: Ln = line n, Cn = column n, En = error n""".stripMargin
      // Note the extra space - it's because we try to align warn and errors.
      // We may want to change that if there are not errors.
    }
  }

  it should "reverse problem order when told to" in {
    val code =
      """    anError
        |moreError""".stripMargin
    val configWithReversedOrder = defaultConfig.withReverseOrder(true)
    val expectedText            = "[E2] /tmp/src.scala"

    collectMessagesFor(code, configWithReversedOrder) { (problems, messages) =>
      problems should have length 2

      messages should have length 4
      val (_, msg) = messages.head
      val lines    = msg.split(EOL)
      lines(0) shouldBe expectedText

      messages.map(_._2).mkString(EOL) shouldBe
        """[E2] /tmp/src.scala
          |     not found: value moreError
          |     L2: moreError
          |         ^
          |[E1] /tmp/src.scala
          |     not found: value anError
          |     L1:     anError
          |             ^
          |/tmp/src.scala: L1 [E1], L2 [E2]
          |Legend: Ln = line n, Cn = column n, En = error n""".stripMargin
    }
  }

  it should "not show the legend when told not to" in {
    val code                = "anError"
    val configWithoutLegend = defaultConfig.withShowLegend(false)
    val expectedText        = "[E1] /tmp/src.scala"

    collectMessagesFor(code, configWithoutLegend) { (problems, messages) =>
      problems should have length 1

      messages should have length 2
      val (_, msg) = messages.head
      val lines    = msg.split(EOL)
      lines(0) shouldBe expectedText
      messages.map(_._2).mkString(EOL) shouldBe
        """[E1] /tmp/src.scala
          |     not found: value anError
          |     L1: anError
          |         ^
          |/tmp/src.scala: L1 [E1]""".stripMargin
    }
  }
}
