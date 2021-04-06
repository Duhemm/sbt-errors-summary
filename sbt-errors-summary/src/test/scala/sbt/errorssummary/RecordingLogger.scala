package sbt
package errorssummary

import scala.collection.mutable

class RecordingLogger extends Logger {

  private val buffer: mutable.Buffer[(Level.Value, String)] =
    mutable.Buffer.empty

  def log(level: Level.Value, message: => String): Unit =
    buffer += ((level, message))
  def success(message: => String): Unit = ()
  def trace(t: => Throwable): Unit      = ()

  def getAll(): Seq[(Level.Value, String)] =
    buffer
}
