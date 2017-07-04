package cross

import xsbti.{Maybe, Reporter}

import scala.reflect.internal.util.{BatchSourceFile, NoFile}
import scala.reflect.internal.util.Position

import scala.tools.nsc.{CompilerCommand, Global}
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.reporters.{AbstractReporter, Reporter => NSCReporter}

/**
 * Helper object to compile code snippets to a virtual directory.
 */
class Compiler(reporter: Reporter) extends CompilerAPI {

  case class CompilationFailed(msg: String) extends Exception(msg)

  /**
   * Compiles the given code, passing the given options to the compiler.
   * Positions will be adapted to be as if the source was in `filePath`.
   */
  def compile(code: String, options: Array[String], filePath: String): Unit = {
    val (wrappedCode, posFn0) = wrap(code)
    val posFn                 = setSourceFile(filePath, posFn0)
    val cpOpt                 = Seq("-cp", sys.props("test.compiler.cp"))
    val global                = getCompiler(posFn, options = cpOpt ++ options: _*)

    import global._
    val source = new BatchSourceFile(NoFile, wrappedCode)
    val run    = new Run
    run.compileSources(source :: Nil)
  }

  private def reportError(error: String) = throw CompilationFailed(error)

  /**
   * Returns an instance of `Global` configured according to the given options.
   * @param posFn   How to transform position to account for code transformation.
   * @param options Options to pass to scalac
   * @return An instance of `Global`.
   */
  private def getCompiler(posFn: xsbti.Position => xsbti.Position,
                          options: String*): Global = {
    // I don't really know how I can reset the compiler after a run, nor what else
    // should also be reset, so for now this method creates new instances of everything,
    // which is not so cool.
    val command     = new CompilerCommand(options.toList, reportError _)
    val nscReporter = xsbt.WrappedReporter(command.settings, reporter, posFn)
    val outputDir   = new VirtualDirectory("(memory)", None)
    command.settings.outputDirs setSingleOutput outputDir

    new Global(command.settings, nscReporter)
  }

  /**
   * Wraps a code snippet in an empty class (scalac refuses to compile isolated code snippets.)
   * Doesn't modify the code if it doesn't need to be wrapped
   */
  private def wrap(code: String): (String, xsbti.Position => xsbti.Position) = {
    if (code startsWith "import") {
      val Array(imp, rest @ _ *) = code.split("\n")
      val (wrapped, fn)          = wrap(rest.mkString("\n"))
      (imp + "\n" + wrapped, fn)
    } else if (code.startsWith("package") || code.startsWith("class") || code
                 .startsWith("object")) {
      (code, identity)
    } else
      (s"""class Compilation {
          |$code
          |}""".stripMargin,
       mapPos(_ - 1, identity))
  }

  /** Sets the source file to `source` in a position. */
  private def setSourceFile(
      source: String,
      fn: xsbti.Position => xsbti.Position): xsbti.Position => xsbti.Position =
    orig =>
      new MyPosition(fn(orig)) {
        override def sourceFile(): xsbti.Maybe[java.io.File] =
          xsbti.Maybe.just(new java.io.File(source))

        override def sourcePath(): xsbti.Maybe[String] =
          xsbti.Maybe.just(source)
    }

  /**
   * Creates a position mapping function
   */
  private def mapPos(lineFn: Int => Int,
                     colFn: Int => Int): xsbti.Position => xsbti.Position =
    new MyPosition(_) {
      override def line(): xsbti.Maybe[Integer] =
        orig.line().map(l => lineFn(l.toInt): Integer)

      override def offset(): xsbti.Maybe[Integer] =
        orig.offset().map(c => colFn(c.toInt): Integer)

      override def pointer(): xsbti.Maybe[Integer] =
        orig.pointer().map(c => colFn(c.toInt): Integer)

      override def pointerSpace(): xsbti.Maybe[String] =
        orig.pointerSpace().map(s => " " * colFn(s.length))
    }

  private class MyPosition(protected val orig: xsbti.Position)
      extends xsbti.Position {
    import scala.language.implicitConversions
    protected implicit def m2o[T](m: Maybe[T]): Option[T] =
      if (m.isDefined) Some(m.get) else None
    protected implicit def o2m[T](o: Option[T]): Maybe[T] =
      o.map(Maybe.just(_)).getOrElse(Maybe.nothing[T])

    def line(): xsbti.Maybe[Integer] =
      orig.line()

    def lineContent(): String =
      orig.lineContent()

    def offset(): xsbti.Maybe[Integer] =
      orig.offset()

    def pointer(): xsbti.Maybe[Integer] =
      orig.pointer()

    def pointerSpace(): xsbti.Maybe[String] =
      orig.pointerSpace()

    def sourceFile(): xsbti.Maybe[java.io.File] =
      orig.sourceFile()

    def sourcePath(): xsbti.Maybe[String] =
      orig.sourcePath()
  }

}
