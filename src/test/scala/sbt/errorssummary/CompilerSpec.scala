package sbt.errorssummary

import compiler.CompilerLoader
import xsbti.Reporter
import org.scalatest.{FlatSpec, Matchers}

import java.util.Optional

trait CompilerSpec {
  protected val scalaVersion = sys.props("test.scala.version")

  def compile(reporter: Reporter, code: String, options: String*): Unit =
    compile(reporter, code, options, Optional.of("/tmp/src.scala"))

  def compile(reporter: Reporter,
              code: String,
              options: Seq[String],
              path: Optional[String]): Unit = {
    val compiler = CompilerLoader.load(reporter)
    compiler.compile(code, options.toArray, path)
  }
}
