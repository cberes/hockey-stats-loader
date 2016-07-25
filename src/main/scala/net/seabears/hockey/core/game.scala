package net.seabears.hockey.core

import java.time.ZonedDateTime

sealed abstract class Game {
  val home: Team
  val away: Team
  val scheduled: ZonedDateTime
}

case class FutureGame(home: Team, away: Team, scheduled: ZonedDateTime) extends Game

case class PastGame(home: Team, away: Team, scheduled: ZonedDateTime) extends Game {
  private[this] val teams: Set[Team] = Set(home, away)
  private[this] var goals: Map[Team, Int] = Map().withDefaultValue(0)
  private[this] var shots: Map[Bucket, Map[GameEvent, Int]] = Map().withDefaultValue(Map().withDefaultValue(0))

  def rawStats(team: Team): Map[Bucket, Map[String, Int]] = {
    shots map {case (bucket, events) => (bucket, events.filterKeys(_.evenStrength).filterKeys(event => event.team == team).map{case (event, value) => (event.getClass.getSimpleName, value)})}
  }

  def score: Map[Team, Int] = (teams map (team => team -> goals(team))).toMap

  def fenwick(bucket: Bucket)(team: Team): Int = fenwickAll(team)(bucket)

  def fenwickAll(team: Team): Map[Bucket, Int] =
    shots map {case (bucket, events) => (bucket -> (events(ShotOnGoal(team, true)) + events(ShotMissed(team, true))))} withDefaultValue(0)

  def fenwickPct(bucket: Bucket)(team: Team): Double = statPct(fenwick, bucket, team)

  def fenwickPctAll(team: Team): Map[Bucket, Double] = statPctAll(fenwickAll, team)

  def corsi(bucket: Bucket)(team: Team): Int = corsiAll(team)(bucket)

  def corsiAll(team: Team): Map[Bucket, Int] =
    shots map {case (bucket, events) => (bucket -> (events(ShotOnGoal(team, true)) + events(ShotMissed(team, true)) + events(ShotBlocked(team, true))))} withDefaultValue(0)

  def corsiPct(bucket: Bucket)(team: Team): Double = statPct(corsi, bucket, team)

  def corsiPctAll(team: Team): Map[Bucket, Double] = statPctAll(corsiAll, team)

  private def statPct(func: Bucket => Team => Double, bucket: Bucket, team: Team): Double =
    func(bucket)(team) / (func(bucket)(team) + func(bucket.getFoil)(getOtherTeam(team))).toDouble

  private def statPctAll(func: Team => Map[Bucket, Int], team: Team): Map[Bucket, Double] = {
    val statFor = func(team)
    val statAgainst = func(getOtherTeam(team))
    shots.keySet.map(bucket => (bucket -> statFor(bucket) / (statFor(bucket) + statAgainst(bucket.getFoil)).toDouble)).filterNot(_._2.isNaN).toMap
  }

  private def getOtherTeam(team: Team): Team = (teams - team).head

  def put(event: GameEvent) = event match {
    case GoalScored(team, evenStrength) => {
      putShot(ShotOnGoal(team, evenStrength))
      goals = goals updated (team, goals(team) + 1)
    }
    case shot => putShot(shot)
  }

  private def putShot(event: GameEvent) = if (event.evenStrength) putEvenStrengthShot(event)

  private def putEvenStrengthShot(event: GameEvent) = {
    val score: Int = goals(event.team)
    val otherScore: Int = goals(getOtherTeam(event.team))
    Bucket.getBuckets(score, otherScore) foreach (bucket => {
      var teamShots: Map[GameEvent, Int] = shots(bucket)
      teamShots = teamShots updated (event, teamShots(event) + 1) 
      shots = shots + (bucket -> teamShots)
    })
  }
}
