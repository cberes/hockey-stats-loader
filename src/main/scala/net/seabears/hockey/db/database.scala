package net.seabears.hockey.db

import net.seabears.hockey.core._

abstract class Database {
  def insert(game: Game, homeId: Int, awayId: Int): Long
  def insert(gameId: Long, homeScore: Int, awayScore: Int): Unit
  def insert(gameId: Long, teamId: Int, statId: Int, value: Double): Unit
  val selectGame: Game => Option[Long]
  val selectScore: Long => Option[(Int, Int)]
  val selectStat: String => Option[Int]
  val selectTeam: Team => Option[Int]
}
