/**
 * This code is generated using [[http://www.scala-sbt.org/contraband/ sbt-contraband]].
 */

// DO NOT EDIT MANUALLY
package sbt.errorssummary
final class ReporterConfig private (
  val colors: Boolean,
  val shortenPaths: Boolean,
  val columnNumbers: Boolean,
  val errorColor: String,
  val warningColor: String,
  val infoColor: String,
  val debugColor: String,
  val sourcePathColor: String,
  val errorIdColor: String) extends Serializable {
  
  
  
  override def equals(o: Any): Boolean = o match {
    case x: ReporterConfig => (this.colors == x.colors) && (this.shortenPaths == x.shortenPaths) && (this.columnNumbers == x.columnNumbers) && (this.errorColor == x.errorColor) && (this.warningColor == x.warningColor) && (this.infoColor == x.infoColor) && (this.debugColor == x.debugColor) && (this.sourcePathColor == x.sourcePathColor) && (this.errorIdColor == x.errorIdColor)
    case _ => false
  }
  override def hashCode: Int = {
    37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (17 + "ReporterConfig".##) + colors.##) + shortenPaths.##) + columnNumbers.##) + errorColor.##) + warningColor.##) + infoColor.##) + debugColor.##) + sourcePathColor.##) + errorIdColor.##)
  }
  override def toString: String = {
    "ReporterConfig(" + colors + ", " + shortenPaths + ", " + columnNumbers + ", " + errorColor + ", " + warningColor + ", " + infoColor + ", " + debugColor + ", " + sourcePathColor + ", " + errorIdColor + ")"
  }
  protected[this] def copy(colors: Boolean = colors, shortenPaths: Boolean = shortenPaths, columnNumbers: Boolean = columnNumbers, errorColor: String = errorColor, warningColor: String = warningColor, infoColor: String = infoColor, debugColor: String = debugColor, sourcePathColor: String = sourcePathColor, errorIdColor: String = errorIdColor): ReporterConfig = {
    new ReporterConfig(colors, shortenPaths, columnNumbers, errorColor, warningColor, infoColor, debugColor, sourcePathColor, errorIdColor)
  }
  def withColors(colors: Boolean): ReporterConfig = {
    copy(colors = colors)
  }
  def withShortenPaths(shortenPaths: Boolean): ReporterConfig = {
    copy(shortenPaths = shortenPaths)
  }
  def withColumnNumbers(columnNumbers: Boolean): ReporterConfig = {
    copy(columnNumbers = columnNumbers)
  }
  def withErrorColor(errorColor: String): ReporterConfig = {
    copy(errorColor = errorColor)
  }
  def withWarningColor(warningColor: String): ReporterConfig = {
    copy(warningColor = warningColor)
  }
  def withInfoColor(infoColor: String): ReporterConfig = {
    copy(infoColor = infoColor)
  }
  def withDebugColor(debugColor: String): ReporterConfig = {
    copy(debugColor = debugColor)
  }
  def withSourcePathColor(sourcePathColor: String): ReporterConfig = {
    copy(sourcePathColor = sourcePathColor)
  }
  def withErrorIdColor(errorIdColor: String): ReporterConfig = {
    copy(errorIdColor = errorIdColor)
  }
}
object ReporterConfig {
  
  def apply(colors: Boolean, shortenPaths: Boolean, columnNumbers: Boolean, errorColor: String, warningColor: String, infoColor: String, debugColor: String, sourcePathColor: String, errorIdColor: String): ReporterConfig = new ReporterConfig(colors, shortenPaths, columnNumbers, errorColor, warningColor, infoColor, debugColor, sourcePathColor, errorIdColor)
}
