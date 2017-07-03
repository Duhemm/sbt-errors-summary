package sbt
package errorssummary
package compiler

import _root_.cross.CompilerAPI
import java.io.File
import java.net.URLClassLoader
import xsbti.Reporter

object CompilerLoader {

  private val allow: String => Boolean =
    n => !n.startsWith("scala.") && !n.startsWith("xsbt.")

  private val classLoader = {
    val parts = sys
      .props("test.compiler.cp")
      .split(File.pathSeparator)
      .map(new java.io.File(_))
      .map(_.toURI.toURL)

    val parent = new FilteredClassLoader(allow, this.getClass.getClassLoader)
    new URLClassLoader(parts, parent)
  }

  def load(reporter: Reporter): CompilerAPI = {
    val clss = classLoader.loadClass("cross.Compiler")
    val ctor = clss.getConstructors()(0)
    ctor.newInstance(reporter) match {
      case compiler: CompilerAPI => compiler
      case other                 => throw new Exception("WTF? " + other.getClass.getName)
    }
  }
}
