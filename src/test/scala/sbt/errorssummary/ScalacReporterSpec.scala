package sbt
package errorssummary

import scala.compat.Platform.EOL
import org.scalatest.{FlatSpec, Matchers}
import xsbti.{Maybe, Severity}

class ScalacReporterSpec
    extends FlatSpec
    with Matchers
    with CompilerSpec
    with ReporterSpec {
  s"The `ScalacReporter` running $scalaVersion" should "collect errors" in collectMessagesFor(
    "foobar") { (problems, messages) =>
    problems should have length 1
    messages should have length 2

    val problem = problems(0)

    problem.severity shouldBe Severity.Error
    problem.position.line.get shouldBe 1

    messages.map(_._2).mkString(EOL) shouldBe
      """/tmp/src.scala:1:1: not found: value foobar
        |foobar
        |^
        |one error found""".stripMargin
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

    messages.map(_._2).mkString(EOL) shouldBe
      """/tmp/src.scala:1:1: not found: value foobar
        |foobar
        |^
        |/tmp/src.scala:2:4: not found: value moreError
        |   moreError
        |   ^
        |/tmp/src.scala:4:6: not found: value eventMore
        |     eventMore
        |     ^
        |three errors found""".stripMargin
  }

  it should "collect problems when imports are present" in collectMessagesFor {
    """import scala.collection.mutable
      |     anError""".stripMargin
  } { (problems, messages) =>
    problems should have length 1
    messages should have length 2

    problems(0).severity shouldBe Severity.Error
    problems(0).position.line.get shouldBe 2

    messages.map(_._2).mkString(EOL) shouldBe
      """/tmp/src.scala:2:6: not found: value anError
        |     anError
        |     ^
        |one error found""".stripMargin
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

    messages.map(_._2).mkString(EOL) shouldBe
      """/tmp/src.scala:2:20: type mismatch;
        | found   : String("hello")
        | required: Int
        |  def foo(): Int = "hello"
        |                   ^
        |one error found""".stripMargin
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
          s"""$deprecationMessage
             |one warning found""".stripMargin
    }
  }

  override def defaultConfig: ReporterConfig =
    super.defaultConfig.withFormat(ScalacFormat)

}
