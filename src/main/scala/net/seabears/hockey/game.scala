package net.seabears.hockey

trait Bucket

case object Even extends Bucket
case object Ahead extends Bucket
case object Behind extends Bucket
case object Close extends Bucket

class Game(val teams: Set[String]) {
  private[this] var goals: Map[String, Int] = Map().withDefaultValue(0)
  private[this] var shots: Map[GameEvent, Map[Bucket, Int]] = Map().withDefaultValue(Map().withDefaultValue(0))

  def score: Map[String, Int] = (teams map (team => team -> goals(team))).toMap

  def fenwick(bucket: Bucket)(team: String): Int =
    shots(ShotOnGoal(team, true))(bucket) + shots(ShotMissed(team, true))(bucket)

  def fenwickPct(bucket: Bucket)(team: String): Double =
    fenwick(bucket)(team) / (fenwick(bucket)(team) + fenwick(bucket)(getOtherTeam(team))).toDouble

  def corsi(bucket: Bucket)(team: String): Int =
    shots(ShotOnGoal(team, true))(bucket) + shots(ShotMissed(team, true))(bucket) + shots(ShotBlocked(getOtherTeam(team), true))(bucket)

  def corsiPct(bucket: Bucket)(team: String): Double =
    corsi(bucket)(team) / (corsi(bucket)(team) + corsi(bucket)(getOtherTeam(team))).toDouble

  private def getOtherTeam(team: String): String = (teams - team).head

  def put(event: GameEvent) = event match {
    case GoalScored(team, evenStrength) => {
      putShot(ShotOnGoal(team, evenStrength))
      goals = goals updated (team, goals(team) + 1)
    }
    case shot => putShot(shot)
  }

  private def putShot(event: GameEvent) = if (event.evenStrength) putEvenStrengthShot(event)

  private def putEvenStrengthShot(event: GameEvent) = {
    getBuckets(event.team) foreach (bucket => {
      var teamShots: Map[Bucket, Int] = shots(event)
      teamShots = teamShots updated (bucket, teamShots(bucket) + 1) 
      shots = shots + (event -> teamShots)
    })
  }

  private def getBuckets(team: String): List[Bucket] = {
    val thisScore: Int = goals(team)
    val otherScore: Int = goals(getOtherTeam(team))
    val diff: Int = thisScore - otherScore

    if (diff == 0)
      List(Close, Even)
    else if (math.abs(diff) == 1)
      List(Close, if (diff > 0) Ahead else Behind)
    else
      List(if (diff > 0) Ahead else Behind)
  }
}
