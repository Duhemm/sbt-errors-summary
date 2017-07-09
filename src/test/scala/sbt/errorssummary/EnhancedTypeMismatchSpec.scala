package sbt.errorssummary

import sbt.Level
import scala.compat.Platform.EOL
import scala.Console._
import org.scalatest.{FlatSpec, Matchers}

class EnhancedTypeMismatchSpec
    extends FlatSpec
    with Matchers
    with CompilerSpec
    with ConciseReporterSpec {

  val coloredConfig = defaultConfig.withColors(true)

  "Type mismatch errors" should "highlight differences" in {
    val code = """def foo(x: Int) = x
                 |foo("hello")""".stripMargin
    collectMessagesFor(code, coloredConfig) { (_, messages) =>
      messages.length shouldBe 2
      messages.forall(_._1 == Level.Error) shouldBe true
      getTypeMismatch(messages.head._2) shouldBe expectedMessage(
        colored(RED, """String("hello")"""),
        colored(GREEN, "Int"))
    }
  }

  it should "highlight differences 2" in {
    val code = """def foo(x: Option[Int]) = x
                 |def bar: Int = 5
                 |foo(bar)""".stripMargin
    collectMessagesFor(code, coloredConfig) { (_, messages) =>
      messages.length shouldBe 2
      messages.forall(_._1 == Level.Error) shouldBe true
      getTypeMismatch(messages.head._2) shouldBe expectedMessage(
        "Int",
        colored(GREEN, "Option[") + "Int" + colored(GREEN, "]"))
    }
  }

  it should "highlight differences 3" in {
    val code = """def foo(x: Option[Int]) = x
                 |foo(List(4))""".stripMargin
    collectMessagesFor(code, coloredConfig) { (_, messages) =>
      messages.length shouldBe 2
      messages.forall(_._1 == Level.Error) shouldBe true
      getTypeMismatch(messages.head._2) shouldBe expectedMessage(
        colored(RED, "List") + "[Int]",
        colored(GREEN, "Option") + "[Int]")
    }
  }

  private def expectedMessage(found: String, required: String): String =
    s"""type mismatch;
       | found   : $found
       | required: $required""".stripMargin

  private def getTypeMismatch(message: String): String =
    message.lines.slice(1, 4).map(_.drop(4)).mkString(EOL)

  private def colored(color: String, text: String): String =
    s"${RESET}${color}${text}${RESET}"
}
