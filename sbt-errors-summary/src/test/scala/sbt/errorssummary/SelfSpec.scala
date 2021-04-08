package sbt.errorssummary

import java.util.Optional

import scala.collection.mutable.Buffer

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import xsbti.Position
import xsbti.Severity

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

  it should "accept leading zero in literals iff we're on 2.10 or 2.13" in {
    val code     = """val x = 0123"""
    val reporter = new BasicReporter
    compile(reporter, code)

    if (scalaVersion.startsWith("2.13.")) {
      reporter.problems() should have length 0
      reporter.hasErrors() shouldBe false
      reporter.hasWarnings() shouldBe false
    } else if (scalaVersion.startsWith("2.10.")) {
      reporter.hasErrors() shouldBe false
      reporter.hasWarnings() shouldBe true
      val problem = reporter.problems()(0)
      problem.severity shouldBe Severity.Warn
      problem.position.line.isPresent shouldBe false
    } else {
      reporter.hasErrors() shouldBe true
      reporter.hasWarnings() shouldBe false
      val problem = reporter.problems()(0)
      problem.severity shouldBe Severity.Error
      problem.position.line.get shouldBe 1
    }
  }

  it should "not support -Ywarn-unused-import iff we're on 2.10" in {
    val code = """val x = 1"""

    if (scalaVersion.startsWith("2.10.") || scalaVersion.startsWith("2.13.")) {
      an[Exception] should be thrownBy compile(
        new BasicReporter,
        code,
        "-Ywarn-unused-import"
      )
    } else {
      noException should be thrownBy compile(
        new BasicReporter,
        code,
        "-Ywarn-unused-import"
      )
    }
  }

  it should "not find `SortedMap` if we're not on 2.12 or 2.13" in {
    val code = """val m = scala.collection.mutable.SortedMap"""

    val reporter = new BasicReporter
    compile(reporter, code)
    if (scalaVersion.startsWith("2.12.") || scalaVersion.startsWith("2.13.")) {
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

  it should "set the sourcefile" in {
    val code       = """error"""
    val reporter   = new BasicReporter
    val sourceFile = "/foo/bar/src.scala"
    compile(reporter, code, Seq.empty, Optional.of(sourceFile))

    reporter.problems should have length 1
    val problem = reporter.problems()(0)
    problem.position.sourceFile.isPresent shouldBe true
    problem.position.sourceFile.get shouldBe new java.io.File(sourceFile)
    problem.position.sourcePath.isPresent shouldBe true
    problem.position.sourcePath.get shouldBe sourceFile
  }

  private class BasicReporter extends xsbti.Reporter {
    val msgs: Buffer[xsbti.Problem] = Buffer.empty

    def comment(pos: Position, msg: String): Unit = ()
    def hasErrors(): Boolean                      = msgs.exists(_.severity == Severity.Error)
    def hasWarnings(): Boolean                    = msgs.exists(_.severity == Severity.Warn)
    def log(problem: xsbti.Problem): Unit =
      msgs += problem
    def printSummary(): Unit = msgs.foreach(println)
    def problems(): Array[xsbti.Problem] =
      msgs.toArray
    def reset(): Unit = msgs.clear()
    private case class BasicProblem(
        severity: Severity,
        position: Position,
        message: String
    ) extends xsbti.Problem {
      def category(): String = ""
    }
  }

}
