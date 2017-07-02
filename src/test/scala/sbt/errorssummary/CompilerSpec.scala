package sbt
package errorssummary

import compiler.CompilerLoader
import xsbti.Reporter
import org.scalatest.{FlatSpec, Matchers}

abstract class CompilerSpec extends FlatSpec with Matchers {
  protected val scalaVersion = sys.props("test.scala.version")

  def compile(reporter: Reporter, code: String): Unit = {
    val compiler = CompilerLoader.load(reporter)
    compiler.compile(code)
  }
}
