package net.seabears.hockey.core

object Bucket {
  def getBuckets(score: Int, otherScore: Int): List[Bucket] = {
    val diff: Int = score - otherScore
    val close: List[Bucket] = if (math.abs(diff) < 2) List(Close) else List()
    List(getScoreBucket(diff)) ++ close
  }

  private def getScoreBucket(diff: Int): Bucket = {
    if (diff == 0) Even
    else if (diff >= 3) Ahead(3)
    else if (diff <= -3) Behind(3)
    else if (diff > 0) Ahead(diff)
    else Behind(math.abs(diff))
  }
}

trait Bucket {
  def getFoil: Bucket = this
}

case object Close extends Bucket
case object Even extends Bucket
case class Ahead(goals: Int) extends Bucket {
  override def getFoil = Behind(goals)
}
case class Behind(goals: Int) extends Bucket {
  override def getFoil = Ahead(goals)
}
