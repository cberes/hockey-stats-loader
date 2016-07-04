package net.seabears.hockey

class MemoizedDatabase(db: Database) extends Database {
  def insert(game: Game): Int = 0
  def insert(gameId: Int, homeScore: Int, awayScore: Int) {}
  def insert(gameId: Int, teamId: Int, statId: Int, value: Double) {}
  val selectGame: MemoizeOpt[Game, Int] = Memoize(db.selectGame)
  val selectScore: MemoizeOpt[Int, (Int, Int)] = Memoize(db.selectScore)
  val selectStat: MemoizeOpt[String, Int] = Memoize(db.selectStat)
  val selectTeam: MemoizeOpt[Team, Int] = Memoize(db.selectTeam)
}
