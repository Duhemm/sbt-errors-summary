package sbt
package errorssummary

import compiler.CompilerLoader
import xsbti.{Maybe, Reporter}
import org.scalatest.{FlatSpec, Matchers}

trait CompilerSpec {
  protected val scalaVersion = sys.props("test.scala.version")

  def compile(reporter: Reporter, code: String, options: String*): Unit =
    compile(reporter, code, options, Maybe.just("/tmp/src.scala"))

  def compile(reporter: Reporter,
              code: String,
              options: Seq[String],
              path: Maybe[String]): Unit = {
    val compiler = CompilerLoader.load(reporter)
    compiler.compile(code, options.toArray, path)
  }
}
