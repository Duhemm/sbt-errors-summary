package sbt
package errorssummary

import org.scalatest.{FlatSpec, Matchers}
import xsbti.{Position, Problem, Severity}
import scala.collection.mutable.Buffer

class SelfSpec extends FlatSpec with Matchers with CompilerSpec {
  "The test framework" should "compile simple snippets" in {
    val code     = """val x = 1 + 2"""
    val reporter = new BasicReporter
    compile(reporter, code)

    reporter.hasErrors() shouldBe false
    reporter.hasWarnings() shouldBe false
  }

  it should "report warnings as such" in {
    // Warning: pure expression in statement position.
    val code     = """1 + 2"""
    val reporter = new BasicReporter
    compile(reporter, code)

    reporter.hasErrors() shouldBe false
    reporter.hasWarnings() shouldBe true
    reporter.problems() should have length 1

    val problem = reporter.problems()(0)
    problem.severity shouldBe Severity.Warn
    problem.position.line.get shouldBe 1
  }

  it should "compile octal literals iff we're on 2.10" in {
    val code     = """val x = 0123"""
    val reporter = new BasicReporter
    compile(reporter, code)

    reporter.problems() should have length 1
    val problem = reporter.problems()(0)
    if (scalaVersion.startsWith("2.10.")) {
      reporter.hasErrors() shouldBe false
      reporter.hasWarnings() shouldBe true
      problem.severity shouldBe Severity.Warn
      problem.position.line.isDefined shouldBe false
    } else {
      reporter.hasErrors() shouldBe true
      reporter.hasWarnings() shouldBe false
      problem.severity shouldBe Severity.Error
      problem.position.line.get shouldBe 1
    }
  }

  it should "not support -Ywarn-unused-import iff we're on 2.10" in {
    val code = """val x = 1"""

    if (scalaVersion.startsWith("2.10.")) {
      an[Exception] should be thrownBy compile(new BasicReporter,
                                               code,
                                               "-Ywarn-unused-import")
    } else {
      noException should be thrownBy compile(new BasicReporter,
                                             code,
                                             "-Ywarn-unused-import")
    }
  }

  it should "not find `SortedMap` if we're not on 2.12" in {
    val code = """val m = scala.collection.mutable.SortedMap"""

    val reporter = new BasicReporter
    compile(reporter, code)
    if (scalaVersion.startsWith("2.12.")) {
      reporter.hasErrors() shouldBe false
      reporter.hasWarnings() shouldBe false
      reporter.problems() should have length 0
    } else {
      reporter.hasErrors() shouldBe true
      reporter.problems() should have length 1
      val problem = reporter.problems()(0)
      problem.severity shouldBe Severity.Error
      problem.position.line.get shouldBe 1
    }
  }

  private class BasicReporter extends xsbti.Reporter {
    val msgs: Buffer[(Severity, Position, String)] = Buffer.empty

    def comment(pos: Position, msg: String): Unit = ()
    def hasErrors(): Boolean                      = msgs.exists(_._1 == Severity.Error)
    def hasWarnings(): Boolean                    = msgs.exists(_._1 == Severity.Warn)
    def log(pos: Position, msg: String, severity: Severity): Unit =
      msgs += ((severity, pos, msg))
    def printSummary(): Unit = msgs.foreach(println)
    def problems(): Array[Problem] =
      msgs.map { case (s, p, m) => BasicProblem(s, p, m) }.toArray
    def reset(): Unit = msgs.clear()
    private case class BasicProblem(severity: Severity,
                                    position: Position,
                                    message: String)
        extends Problem {
      def category(): String = ""
    }
  }

}
