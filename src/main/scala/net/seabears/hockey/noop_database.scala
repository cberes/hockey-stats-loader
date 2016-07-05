package net.seabears.hockey

object NoopDatabase extends Database {
  def insert(game: Game, homeId: Int, awayId: Int): Long = 0
  def insert(gameId: Long, homeScore: Int, awayScore: Int) {}
  def insert(gameId: Long, teamId: Int, statId: Int, value: Double) {}
  val selectGame: Game => Option[Long] = _ => None
  val selectScore: Long => Option[(Int, Int)] = _ => None
  val selectStat: String => Option[Int] = _ => Some(0)
  val selectTeam: Team => Option[Int] = _ => Some(0)
}
