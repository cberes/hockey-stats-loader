package net.seabears.hockey

object NoopDatabase extends Database {
  def insert(game: Game): Int = 0
  def insert(gameId: Int, homeScore: Int, awayScore: Int) {}
  def insert(gameId: Int, teamId: Int, statId: Int, value: Double) {}
  val selectGame: Game => Option[Int] = _ => None
  val selectScore: Int => Option[(Int, Int)] = _ => None
  val selectStat: String => Option[Int] = _ => Some(0)
  val selectTeam: Team => Option[Int] = _ => Some(0)
}
