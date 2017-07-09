package sbt.errorssummary

import scala.collection.mutable

// Adapted from simplediff
// https://github.com/bluebird75/simplediff

sealed trait DiffOp[T]
object DiffOp {
  case class Add[T](es: Seq[T])    extends DiffOp[T]
  case class Delete[T](es: Seq[T]) extends DiffOp[T]
  case class Equal[T](es: Seq[T])  extends DiffOp[T]
}

object Diff {
  def apply[T](olds: Seq[T], news: Seq[T]): Seq[DiffOp[T]] = {
    val oldIndices = mutable.Map.empty[T, Seq[Int]]
    olds.zipWithIndex.foreach {
      case (oldE, i) =>
        val indices = oldIndices.getOrElse(oldE, Seq.empty)
        oldIndices += oldE -> (indices :+ i)
    }

    var subStartOld = 0
    var subStartNew = 0
    var subLength   = 0
    var overlap     = Map.empty[Int, Int]

    news.zipWithIndex.foreach {
      case (e, newIndex) =>
        val _overlap = mutable.Map.empty[Int, Int]

        for {
          indices  <- oldIndices.get(e);
          oldIndex <- indices
        } {
          if (oldIndex == 0) _overlap += oldIndex -> 1
          else _overlap += oldIndex               -> (overlap.getOrElse(oldIndex - 1, 0) + 1)

          if (_overlap(oldIndex) > subLength) {
            subLength = _overlap(oldIndex)
            subStartOld = oldIndex - subLength + 1
            subStartNew = newIndex - subLength + 1
          }
        }

        overlap = _overlap.toMap
    }

    if (subLength == 0) {
      val inOlds = if (olds.length > 0) DiffOp.Delete(olds) :: Nil else Nil
      val inNews = if (news.length > 0) DiffOp.Add(news) :: Nil else Nil
      inOlds ++ inNews
    } else {
      Diff(olds.slice(0, subStartOld), news.slice(0, subStartNew)) ++
        Seq(DiffOp.Equal(news.slice(subStartNew, subStartNew + subLength))) ++
        Diff(olds.slice(subStartOld + subLength, olds.length),
             news.slice(subStartNew + subLength, news.length))
    }
  }

  def showDiff(before: Seq[String], after: Seq[String])(
      showDelete: String => String = identity,
      showAdd: String => String = identity,
      showEqual: String => String = identity): (String, String) = {
    val diff = Diff(before, after)
    val removed = diff.collect {
      case DiffOp.Delete(chrs) => showDelete(chrs.mkString)
      case DiffOp.Equal(chrs)  => showEqual(chrs.mkString)
    }.mkString
    val added = diff.collect {
      case DiffOp.Add(chrs)   => showAdd(chrs.mkString)
      case DiffOp.Equal(chrs) => showEqual(chrs.mkString)
    }.mkString

    (removed, added)
  }

}
