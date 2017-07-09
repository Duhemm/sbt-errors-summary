package sbt.errorssummary
package messages

import java.util.StringTokenizer

import scala.Console.{GREEN, RED}
import scala.util.matching.Regex

object TypeMismatchError {

  def unapply(str: String): Option[(String, String)] = {
    val regex = message("(.+?)", "(.+?)").r
    str match {
      case regex(found, expected) => Some((found, expected))
      case _                      => None
    }
  }

  def highlight(err: String,
                showDelete: String => String,
                showAdd: String => String): String =
    err match {
      case TypeMismatchError(found, required) =>
        val separators = " ([{.}])"
        val foundTokens = stringTokenizerToSeq(
          new StringTokenizer(found, separators, true))
        val requiredTokens = stringTokenizerToSeq(
          new StringTokenizer(required, separators, true))
        val (highlightedFound, highlightedRequired) =
          Diff.showDiff(foundTokens, requiredTokens)(showDelete, showAdd)
        message(highlightedFound, highlightedRequired)

      case _ =>
        err
    }

  private def stringTokenizerToSeq(tokenizer: StringTokenizer): Seq[String] = {
    val buffer = scala.collection.mutable.Buffer.empty[String]
    while (tokenizer.hasMoreTokens) buffer += tokenizer.nextToken
    buffer
  }

  private def message(found: String, required: String): String =
    s"""type mismatch;
       | found   : ${found}
       | required: ${required}""".stripMargin
}
