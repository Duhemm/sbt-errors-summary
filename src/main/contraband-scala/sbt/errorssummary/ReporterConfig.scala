/**
 * This code is generated using [[http://www.scala-sbt.org/contraband/ sbt-contraband]].
 */
// DO NOT EDIT MANUALLY
package sbt.errorssummary
final class ReporterConfig private (val colors: Boolean,
                                    val shortenPaths: Boolean)
    extends Serializable {

  override def equals(o: Any): Boolean = o match {
    case x: ReporterConfig =>
      (this.colors == x.colors) && (this.shortenPaths == x.shortenPaths)
    case _ => false
  }
  override def hashCode: Int = {
    37 * (37 * (37 * (17 + "ReporterConfig".##) + colors.##) + shortenPaths.##)
  }
  override def toString: String = {
    "ReporterConfig(" + colors + ", " + shortenPaths + ")"
  }
  protected[this] def copy(
      colors: Boolean = colors,
      shortenPaths: Boolean = shortenPaths): ReporterConfig = {
    new ReporterConfig(colors, shortenPaths)
  }
  def withColors(colors: Boolean): ReporterConfig = {
    copy(colors = colors)
  }
  def withShortenPaths(shortenPaths: Boolean): ReporterConfig = {
    copy(shortenPaths = shortenPaths)
  }
}
object ReporterConfig {

  def apply(colors: Boolean, shortenPaths: Boolean): ReporterConfig =
    new ReporterConfig(colors, shortenPaths)
}
