package sbt.errorssummary

import org.scalatest.{FlatSpec, Matchers}

class DiffSpec extends FlatSpec with Matchers {

  diffTest("old", "new", Seq(del("old"), add("new")))
  diffTest("hey", "hoy", Seq(eq("h"), del("e"), add("o"), eq("y")))
  diffTest("Int", "Option[Int]", Seq(add("Option["), eq("Int"), add("]")))
  diffTest("Option[Int]", "Int", Seq(del("Option["), eq("Int"), del("]")))
  diffTest("Option[Int]",
           "Array[Int]",
           Seq(del("Option"), add("Array"), eq("[Int]")))

  private def del(s: String) = DiffOp.Delete(s.toSeq)
  private def add(s: String) = DiffOp.Add(s.toSeq)
  private def eq(s: String)  = DiffOp.Equal(s.toSeq)
  private def diffTest(s1: String, s2: String, exp: Seq[DiffOp[Char]]) =
    it should s"be correct for `$s1` vs `$s2`" in {
      Diff(s1, s2) shouldBe exp
    }
}
