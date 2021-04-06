/**
 * This code is generated using [[http://www.scala-sbt.org/contraband/ sbt-contraband]].
 */

// DO NOT EDIT MANUALLY
package sbt.errorssummary
/**
 * General configuration for the formats.
 * Formats are supposed to be opinionated, and they are free to ignore any
 * of those settings.
 * @param colors `true` to enable colors, `false` to disable them.
 * @param shortenPaths `true` to strip the base directory, `false` to show the full path.
 * @param columnNumbers `true` to show the column number, `false` to hide it.
 * @param reverseOrder `true` to show the errors in reverse order, `false` to show them in FIFO order.
 * @param showLegend `true` to show a legend explaining the output of the reporter, `false` to hide it.
 * @param errorColor The color to use to show errors.
 * @param warningColor The color to use to show warnings.
 * @param infoColor The color to use to show information messages.
 * @param debugColor The color to use to show debug messages.
 * @param sourcePathColor The color to use to highlight the path where a message was triggered.
 * @param errorIdColor The color to use to show an error ID.
 * @param format The format to use.
 */
final class ReporterConfig private (
  val colors: Boolean,
  val shortenPaths: Boolean,
  val columnNumbers: Boolean,
  val reverseOrder: Boolean,
  val showLegend: Boolean,
  val errorColor: String,
  val warningColor: String,
  val infoColor: String,
  val debugColor: String,
  val sourcePathColor: String,
  val errorIdColor: String,
  val format: ReporterFormatFactory) extends Serializable {
  
  private def this() = this(true, true, false, false, true, scala.Console.RED, scala.Console.YELLOW, scala.Console.CYAN, scala.Console.WHITE, scala.Console.UNDERLINED, scala.Console.BLUE, sbt.errorssummary.DefaultReporterFormat)
  private def this(colors: Boolean, shortenPaths: Boolean, columnNumbers: Boolean) = this(colors, shortenPaths, columnNumbers, false, true, scala.Console.RED, scala.Console.YELLOW, scala.Console.CYAN, scala.Console.WHITE, scala.Console.UNDERLINED, scala.Console.BLUE, sbt.errorssummary.DefaultReporterFormat)
  private def this(colors: Boolean, shortenPaths: Boolean, columnNumbers: Boolean, reverseOrder: Boolean, showLegend: Boolean, errorColor: String, warningColor: String, infoColor: String, debugColor: String, sourcePathColor: String, errorIdColor: String) = this(colors, shortenPaths, columnNumbers, reverseOrder, showLegend, errorColor, warningColor, infoColor, debugColor, sourcePathColor, errorIdColor, sbt.errorssummary.DefaultReporterFormat)
  
  override def equals(o: Any): Boolean = o match {
    case x: ReporterConfig => (this.colors == x.colors) && (this.shortenPaths == x.shortenPaths) && (this.columnNumbers == x.columnNumbers) && (this.reverseOrder == x.reverseOrder) && (this.showLegend == x.showLegend) && (this.errorColor == x.errorColor) && (this.warningColor == x.warningColor) && (this.infoColor == x.infoColor) && (this.debugColor == x.debugColor) && (this.sourcePathColor == x.sourcePathColor) && (this.errorIdColor == x.errorIdColor) && (this.format == x.format)
    case _ => false
  }
  override def hashCode: Int = {
    37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (17 + "sbt.errorssummary.ReporterConfig".##) + colors.##) + shortenPaths.##) + columnNumbers.##) + reverseOrder.##) + showLegend.##) + errorColor.##) + warningColor.##) + infoColor.##) + debugColor.##) + sourcePathColor.##) + errorIdColor.##) + format.##)
  }
  override def toString: String = {
    "ReporterConfig(" + colors + ", " + shortenPaths + ", " + columnNumbers + ", " + reverseOrder + ", " + showLegend + ", " + errorColor + ", " + warningColor + ", " + infoColor + ", " + debugColor + ", " + sourcePathColor + ", " + errorIdColor + ", " + format + ")"
  }
  private[this] def copy(colors: Boolean = colors, shortenPaths: Boolean = shortenPaths, columnNumbers: Boolean = columnNumbers, reverseOrder: Boolean = reverseOrder, showLegend: Boolean = showLegend, errorColor: String = errorColor, warningColor: String = warningColor, infoColor: String = infoColor, debugColor: String = debugColor, sourcePathColor: String = sourcePathColor, errorIdColor: String = errorIdColor, format: ReporterFormatFactory = format): ReporterConfig = {
    new ReporterConfig(colors, shortenPaths, columnNumbers, reverseOrder, showLegend, errorColor, warningColor, infoColor, debugColor, sourcePathColor, errorIdColor, format)
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
  def withReverseOrder(reverseOrder: Boolean): ReporterConfig = {
    copy(reverseOrder = reverseOrder)
  }
  def withShowLegend(showLegend: Boolean): ReporterConfig = {
    copy(showLegend = showLegend)
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
  def withFormat(format: ReporterFormatFactory): ReporterConfig = {
    copy(format = format)
  }
}
object ReporterConfig {
  
  def apply(): ReporterConfig = new ReporterConfig()
  def apply(colors: Boolean, shortenPaths: Boolean, columnNumbers: Boolean): ReporterConfig = new ReporterConfig(colors, shortenPaths, columnNumbers)
  def apply(colors: Boolean, shortenPaths: Boolean, columnNumbers: Boolean, reverseOrder: Boolean, showLegend: Boolean, errorColor: String, warningColor: String, infoColor: String, debugColor: String, sourcePathColor: String, errorIdColor: String): ReporterConfig = new ReporterConfig(colors, shortenPaths, columnNumbers, reverseOrder, showLegend, errorColor, warningColor, infoColor, debugColor, sourcePathColor, errorIdColor)
  def apply(colors: Boolean, shortenPaths: Boolean, columnNumbers: Boolean, reverseOrder: Boolean, showLegend: Boolean, errorColor: String, warningColor: String, infoColor: String, debugColor: String, sourcePathColor: String, errorIdColor: String, format: ReporterFormatFactory): ReporterConfig = new ReporterConfig(colors, shortenPaths, columnNumbers, reverseOrder, showLegend, errorColor, warningColor, infoColor, debugColor, sourcePathColor, errorIdColor, format)
}
