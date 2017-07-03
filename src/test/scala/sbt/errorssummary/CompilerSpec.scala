package sbt
package errorssummary

import compiler.CompilerLoader
import xsbti.Reporter
import org.scalatest.{FlatSpec, Matchers}

trait CompilerSpec {
  protected val scalaVersion = sys.props("test.scala.version")

  def compile(reporter: Reporter, code: String, options: String*): Unit = {
    val compiler = CompilerLoader.load(reporter)
    compiler.compile(code, options: _*)
  }
}
