package net.seabears.hockey

sealed abstract class GameEvent {
  val team: String
  val evenStrength: Boolean
}

final case class ShotBlocked(team: String, evenStrength: Boolean) extends GameEvent
final case class ShotMissed(team: String, evenStrength: Boolean) extends GameEvent
final case class ShotOnGoal(team: String, evenStrength: Boolean) extends GameEvent
final case class GoalScored(team: String, evenStrength: Boolean) extends GameEvent

