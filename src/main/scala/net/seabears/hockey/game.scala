package net.seabears.hockey

import java.time.LocalDateTime

class Game(val home: Team, val away: Team, val when: LocalDateTime) {
  private[this] val teams: Set[Team] = Set(home, away)
  private[this] var goals: Map[Team, Int] = Map().withDefaultValue(0)
  private[this] var shots: Map[Bucket, Map[GameEvent, Int]] = Map().withDefaultValue(Map().withDefaultValue(0))

  def rawStats(team: Team): Map[Bucket, Map[String, Int]] = {
    shots map {case (bucket, events) => (bucket, events.filterKeys(_.evenStrength).filterKeys(event => event.team == team).map{case (event, value) => (event.getClass.getSimpleName, value)})}
  }

  def score: Map[Team, Int] = (teams map (team => team -> goals(team))).toMap

  def fenwick(bucket: Bucket)(team: Team): Int =
    shots(bucket)(ShotOnGoal(team, true)) + shots(bucket)(ShotMissed(team, true))

  def fenwickAll(team: Team): Map[Bucket, Int] =
    shots map {case (bucket, events) => (bucket -> (events(ShotOnGoal(team, true)) + events(ShotMissed(team, true))))}

  def fenwickPct(bucket: Bucket)(team: Team): Double =
    fenwick(bucket)(team) / (fenwick(bucket)(team) + fenwick(bucket.getFoil)(getOtherTeam(team))).toDouble

  def fenwickPctAll(team: Team): Map[Bucket, Double] = {
    val fenwickFor = fenwickAll(team)
    val fenwickAgainst = fenwickAll(getOtherTeam(team))
    shots.keySet.map(bucket => (bucket -> fenwickFor(bucket) / (fenwickFor(bucket) + fenwickAgainst(bucket.getFoil)).toDouble)).filterNot(_._2.isNaN).toMap
  }

  def corsi(bucket: Bucket)(team: Team): Int =
    shots(bucket)(ShotOnGoal(team, true)) + shots(bucket)(ShotMissed(team, true)) + shots(bucket)(ShotBlocked(team, true))

  def corsiAll(team: Team): Map[Bucket, Int] =
    shots map {case (bucket, events) => (bucket -> (events(ShotOnGoal(team, true)) + events(ShotMissed(team, true)) + events(ShotBlocked(team, true))))}

  def corsiPct(bucket: Bucket)(team: Team): Double =
    corsi(bucket)(team) / (corsi(bucket)(team) + corsi(bucket.getFoil)(getOtherTeam(team))).toDouble

  def corsiPctAll(team: Team): Map[Bucket, Double] = {
    val corsiFor = corsiAll(team)
    val corsiAgainst = corsiAll(getOtherTeam(team))
    shots.keySet.map(bucket => (bucket -> corsiFor(bucket) / (corsiFor(bucket) + corsiAgainst(bucket.getFoil)).toDouble)).filterNot(_._2.isNaN).toMap
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
