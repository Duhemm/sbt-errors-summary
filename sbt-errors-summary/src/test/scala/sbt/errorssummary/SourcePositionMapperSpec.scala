package sbt.errorssummary

import java.io.File
import java.util.Optional

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import xsbti.Position
class SourcePositionMapperSpec
    extends FlatSpec
    with Matchers
    with CompilerSpec
    with ReporterSpec {
  "Source position mappers" should "be applied to messages received by the reporter" in {
    val code = """error"""
    val linePlus10: Position => Position = orig =>
      new MyPosition(orig) {
        override def line(): Optional[Integer] =
          orig.line.map(_ + 10)
      }
    val logger = new RecordingLogger
    val reporter =
      new Reporter(logger, "/tmp/", linePlus10, defaultConfig)
    compile(reporter, code, Seq.empty, Optional.of("/tmp/src.scala"))

    reporter.hasErrors() shouldBe true
    reporter.problems() should have length 1

    val problem = reporter.problems()(0)
    problem.position.line().get() shouldBe 11
  }
}

private abstract class MyPosition(orig: Position) extends Position {
  def offset(): Optional[Integer]      = orig.offset()
  def line(): Optional[Integer]        = orig.line()
  def lineContent(): String            = orig.lineContent()
  def pointer(): Optional[Integer]     = orig.pointer()
  def pointerSpace(): Optional[String] = orig.pointerSpace()
  def sourceFile(): Optional[File]     = orig.sourceFile()
  def sourcePath(): Optional[String]   = orig.sourcePath()
}
