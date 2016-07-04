package net.seabears.hockey

abstract class Database {
  def insert(game: Game): Int
  def insert(gameId: Int, homeScore: Int, awayScore: Int): Unit
  def insert(gameId: Int, teamId: Int, statId: Int, value: Double): Unit
  val selectGame: Game => Option[Int]
  val selectScore: Int => Option[(Int, Int)]
  val selectStat: String => Option[Int]
  val selectTeam: Team => Option[Int]
}
