package sbt.errorssummary.messages

import org.scalatest.{FlatSpec, Matchers}

class TypeMismatchErrorSpec extends FlatSpec with Matchers {
  "A type mismatch error" should "be recognized as such" in {
    val error = typeMismatch("type0", "type1")

    error match {
      case TypeMismatchError(found, required) =>
        found shouldBe "type0"
        required shouldBe "type1"
      case _ =>
        fail("Couldn't recognize type mismatch error.")
    }
  }

  it should "highlight differences" in {
    val foundType    = "Int"
    val requiredType = "String"
    val error        = typeMismatch(foundType, requiredType)
    TypeMismatchError.highlight(error,
                                showDelete = _.toUpperCase,
                                showAdd = _.reverse) match {
      case TypeMismatchError(hFound, hRequired) =>
        hFound shouldBe foundType.toUpperCase
        hRequired shouldBe requiredType.reverse
    }
  }

  it should "highlight differences 2" in {
    val foundType    = "Option[Int]"
    val requiredType = "Option[String]"
    val error        = typeMismatch(foundType, requiredType)
    TypeMismatchError.highlight(error,
                                showDelete = _.toUpperCase,
                                showAdd = _.reverse) match {
      case TypeMismatchError(hFound, hRequired) =>
        hFound shouldBe "Option[INT]"
        hRequired shouldBe "Option[gnirtS]"
    }
  }

  it should "highlight differences 3" in {
    val foundType    = "Option[Int]"
    val requiredType = "List[Int]"
    val error        = typeMismatch(foundType, requiredType)
    TypeMismatchError.highlight(error,
                                showDelete = _.toUpperCase,
                                showAdd = _.reverse) match {
      case TypeMismatchError(hFound, hRequired) =>
        hFound shouldBe "OPTION[Int]"
        hRequired shouldBe "tsiL[Int]"
    }
  }

  private def typeMismatch(found: String, required: String): String =
    s"""type mismatch;
       | found   : $found
       | required: $required""".stripMargin
}
