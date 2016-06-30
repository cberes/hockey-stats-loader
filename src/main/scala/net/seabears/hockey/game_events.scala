package net.seabears.hockey

sealed abstract class GameEvent {
  val team: Team
  val evenStrength: Boolean
}

final case class ShotBlocked(team: Team, evenStrength: Boolean) extends GameEvent
final case class ShotMissed(team: Team, evenStrength: Boolean) extends GameEvent
final case class ShotOnGoal(team: Team, evenStrength: Boolean) extends GameEvent
final case class GoalScored(team: Team, evenStrength: Boolean) extends GameEvent

