package sbt.errorssummary

import org.scalatest.{FlatSpec, Matchers}
import xsbti.{Position, Problem, Severity}
import scala.collection.mutable.Buffer

import java.util.Optional

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
      problem.position.line.isPresent shouldBe false
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
    val msgs: Buffer[Problem] = Buffer.empty

    def comment(pos: Position, msg: String): Unit = ()
    def hasErrors(): Boolean                      = msgs.exists(_.severity == Severity.Error)
    def hasWarnings(): Boolean                    = msgs.exists(_.severity == Severity.Warn)
    def log(problem: Problem): Unit =
      msgs += problem
    def printSummary(): Unit = msgs.foreach(println)
    def problems(): Array[Problem] =
      msgs.toArray
    def reset(): Unit = msgs.clear()
    private case class BasicProblem(severity: Severity,
                                    position: Position,
                                    message: String)
        extends Problem {
      def category(): String = ""
    }
  }

}
