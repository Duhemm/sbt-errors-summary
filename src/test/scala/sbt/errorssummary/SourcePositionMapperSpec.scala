package sbt
package errorssummary

import xsbti.{Maybe, Position}

import org.scalatest.{FlatSpec, Matchers}
class SourcePositionMapperSpec
    extends FlatSpec
    with Matchers
    with CompilerSpec
    with ReporterSpec {
  "Source position mappers" should "be applied to messages received by the reporter" in {
    val code = """error"""
    val linePlus10: Position => Position = orig =>
      new MyPosition(orig) {
        override def line(): Maybe[Integer] =
          if (orig.line().isDefined) Maybe.just(orig.line.get() + 10)
          else Maybe.nothing[Integer]
    }
    val logger = new RecordingLogger
    val reporter =
      new Reporter(logger, "/tmp/", None, linePlus10, defaultConfig)
    compile(reporter, code, Seq.empty, Maybe.just("/tmp/src.scala"))

    reporter.hasErrors() shouldBe true
    reporter.problems() should have length 1

    val problem = reporter.problems()(0)
    problem.position.line().get() shouldBe 11
  }
}

private abstract class MyPosition(orig: Position) extends Position {
  def offset(): Maybe[Integer]      = orig.offset()
  def line(): Maybe[Integer]        = orig.line()
  def lineContent(): String         = orig.lineContent()
  def pointer(): Maybe[Integer]     = orig.pointer()
  def pointerSpace(): Maybe[String] = orig.pointerSpace()
  def sourceFile(): Maybe[File]     = orig.sourceFile()
  def sourcePath(): Maybe[String]   = orig.sourcePath()
}
