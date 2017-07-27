/**
 * This code is generated using [[http://www.scala-sbt.org/contraband/ sbt-contraband]].
 */
// DO NOT EDIT MANUALLY
package sbt.errorssummary

/**
 * General configuration for the formats.
 * Formats are supposed to be opinionated, and they are free to ignore any
 * of those settings.
 */
final class ReporterConfig private (
                                    /** `true` to enable colors, `false` to disable them. */
                                    val colors: Boolean,
                                    /** `true` to strip the base directory, `false` to show the full path. */
                                    val shortenPaths: Boolean,
                                    /** `true` to show the column number, `false` to hide it. */
                                    val columnNumbers: Boolean,
                                    /** `true` to show the errors in reverse order, `false` to show them in FIFO order. */
                                    val reverseOrder: Boolean,
                                    /** `true` to show a legend explaining the output of the reporter, `false` to hide it. */
                                    val showLegend: Boolean,
                                    /** The color to use to show errors. */
                                    val errorColor: String,
                                    /** The color to use to show warnings. */
                                    val warningColor: String,
                                    /** The color to use to show information messages. */
                                    val infoColor: String,
                                    /** The color to use to show debug messages. */
                                    val debugColor: String,
                                    /** The color to use to highlight the path where a message was triggered. */
                                    val sourcePathColor: String,
                                    /** The color to use to show an error ID. */
                                    val errorIdColor: String,
                                    /** The format to use. */
                                    val format: ReporterFormatFactory)
    extends Serializable {

  private def this() =
    this(
      true,
      true,
      false,
      false,
      true,
      scala.Console.RED,
      scala.Console.YELLOW,
      scala.Console.CYAN,
      scala.Console.WHITE,
      scala.Console.UNDERLINED,
      scala.Console.BLUE,
      sbt.errorssummary.DefaultReporterFormat
    )
  private def this(colors: Boolean,
                   shortenPaths: Boolean,
                   columnNumbers: Boolean) =
    this(
      colors,
      shortenPaths,
      columnNumbers,
      false,
      true,
      scala.Console.RED,
      scala.Console.YELLOW,
      scala.Console.CYAN,
      scala.Console.WHITE,
      scala.Console.UNDERLINED,
      scala.Console.BLUE,
      sbt.errorssummary.DefaultReporterFormat
    )
  private def this(colors: Boolean,
                   shortenPaths: Boolean,
                   columnNumbers: Boolean,
                   reverseOrder: Boolean,
                   showLegend: Boolean,
                   errorColor: String,
                   warningColor: String,
                   infoColor: String,
                   debugColor: String,
                   sourcePathColor: String,
                   errorIdColor: String) =
    this(
      colors,
      shortenPaths,
      columnNumbers,
      reverseOrder,
      showLegend,
      errorColor,
      warningColor,
      infoColor,
      debugColor,
      sourcePathColor,
      errorIdColor,
      sbt.errorssummary.DefaultReporterFormat
    )

  override def equals(o: Any): Boolean = o match {
    case x: ReporterConfig =>
      (this.colors == x.colors) && (this.shortenPaths == x.shortenPaths) && (this.columnNumbers == x.columnNumbers) && (this.reverseOrder == x.reverseOrder) && (this.showLegend == x.showLegend) && (this.errorColor == x.errorColor) && (this.warningColor == x.warningColor) && (this.infoColor == x.infoColor) && (this.debugColor == x.debugColor) && (this.sourcePathColor == x.sourcePathColor) && (this.errorIdColor == x.errorIdColor) && (this.format == x.format)
    case _ => false
  }
  override def hashCode: Int = {
    37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (37 * (17 + "ReporterConfig".##) + colors.##) + shortenPaths.##) + columnNumbers.##) + reverseOrder.##) + showLegend.##) + errorColor.##) + warningColor.##) + infoColor.##) + debugColor.##) + sourcePathColor.##) + errorIdColor.##) + format.##)
  }
  override def toString: String = {
    "ReporterConfig(" + colors + ", " + shortenPaths + ", " + columnNumbers + ", " + reverseOrder + ", " + showLegend + ", " + errorColor + ", " + warningColor + ", " + infoColor + ", " + debugColor + ", " + sourcePathColor + ", " + errorIdColor + ", " + format + ")"
  }
  protected[this] def copy(
      colors: Boolean = colors,
      shortenPaths: Boolean = shortenPaths,
      columnNumbers: Boolean = columnNumbers,
      reverseOrder: Boolean = reverseOrder,
      showLegend: Boolean = showLegend,
      errorColor: String = errorColor,
      warningColor: String = warningColor,
      infoColor: String = infoColor,
      debugColor: String = debugColor,
      sourcePathColor: String = sourcePathColor,
      errorIdColor: String = errorIdColor,
      format: ReporterFormatFactory = format): ReporterConfig = {
    new ReporterConfig(colors,
                       shortenPaths,
                       columnNumbers,
                       reverseOrder,
                       showLegend,
                       errorColor,
                       warningColor,
                       infoColor,
                       debugColor,
                       sourcePathColor,
                       errorIdColor,
                       format)
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

  def apply(): ReporterConfig =
    new ReporterConfig(
      true,
      true,
      false,
      false,
      true,
      scala.Console.RED,
      scala.Console.YELLOW,
      scala.Console.CYAN,
      scala.Console.WHITE,
      scala.Console.UNDERLINED,
      scala.Console.BLUE,
      sbt.errorssummary.DefaultReporterFormat
    )
  def apply(colors: Boolean,
            shortenPaths: Boolean,
            columnNumbers: Boolean): ReporterConfig =
    new ReporterConfig(
      colors,
      shortenPaths,
      columnNumbers,
      false,
      true,
      scala.Console.RED,
      scala.Console.YELLOW,
      scala.Console.CYAN,
      scala.Console.WHITE,
      scala.Console.UNDERLINED,
      scala.Console.BLUE,
      sbt.errorssummary.DefaultReporterFormat
    )
  def apply(colors: Boolean,
            shortenPaths: Boolean,
            columnNumbers: Boolean,
            reverseOrder: Boolean,
            showLegend: Boolean,
            errorColor: String,
            warningColor: String,
            infoColor: String,
            debugColor: String,
            sourcePathColor: String,
            errorIdColor: String): ReporterConfig =
    new ReporterConfig(
      colors,
      shortenPaths,
      columnNumbers,
      reverseOrder,
      showLegend,
      errorColor,
      warningColor,
      infoColor,
      debugColor,
      sourcePathColor,
      errorIdColor,
      sbt.errorssummary.DefaultReporterFormat
    )
  def apply(colors: Boolean,
            shortenPaths: Boolean,
            columnNumbers: Boolean,
            reverseOrder: Boolean,
            showLegend: Boolean,
            errorColor: String,
            warningColor: String,
            infoColor: String,
            debugColor: String,
            sourcePathColor: String,
            errorIdColor: String,
            format: ReporterFormatFactory): ReporterConfig =
    new ReporterConfig(colors,
                       shortenPaths,
                       columnNumbers,
                       reverseOrder,
                       showLegend,
                       errorColor,
                       warningColor,
                       infoColor,
                       debugColor,
                       sourcePathColor,
                       errorIdColor,
                       format)
}
