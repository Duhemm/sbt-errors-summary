/**
 * This code is generated using [[http://www.scala-sbt.org/contraband/ sbt-contraband]].
 */
// DO NOT EDIT MANUALLY
package sbt.errorssummary

/** Describes a problem (error, warning, message, etc.) given to the reporter. */
final class Problem private (
                             /** A unique (per compilation run) number for this message. */
                             val id: Int,
                             /** The severity of this message. */
                             val severity: xsbti.Severity,
                             /** The actual content of the message */
                             val message: String,
                             /** Position in the source code where the message was triggered */
                             val position: xsbti.Position,
                             /** The category of this problem. */
                             val category: String)
    extends xsbti.Problem
    with Serializable {

  override def equals(o: Any): Boolean = o match {
    case x: Problem =>
      (this.id == x.id) && (this.severity == x.severity) && (this.message == x.message) && (this.position == x.position) && (this.category == x.category)
    case _ => false
  }
  override def hashCode: Int = {
    37 * (37 * (37 * (37 * (37 * (37 * (17 + "Problem".##) + id.##) + severity.##) + message.##) + position.##) + category.##)
  }
  override def toString: String = {
    "Problem(" + id + ", " + severity + ", " + message + ", " + position + ", " + category + ")"
  }
  protected[this] def copy(id: Int = id,
                           severity: xsbti.Severity = severity,
                           message: String = message,
                           position: xsbti.Position = position,
                           category: String = category): Problem = {
    new Problem(id, severity, message, position, category)
  }
  def withId(id: Int): Problem = {
    copy(id = id)
  }
  def withSeverity(severity: xsbti.Severity): Problem = {
    copy(severity = severity)
  }
  def withMessage(message: String): Problem = {
    copy(message = message)
  }
  def withPosition(position: xsbti.Position): Problem = {
    copy(position = position)
  }
  def withCategory(category: String): Problem = {
    copy(category = category)
  }
}
object Problem {

  def apply(id: Int,
            severity: xsbti.Severity,
            message: String,
            position: xsbti.Position,
            category: String): Problem =
    new Problem(id, severity, message, position, category)
}
