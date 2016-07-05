package net.seabears.hockey

class MemoizedDatabase(db: Database) extends Database {
  def insert(game: Game, homeId: Int, awayId: Int): Long = db.insert(game, homeId, awayId)
  def insert(gameId: Long, homeScore: Int, awayScore: Int) = db.insert(gameId, homeScore, awayScore)
  def insert(gameId: Long, teamId: Int, statId: Int, value: Double) = db.insert(gameId, teamId, statId, value)
  val selectGame: MemoizeOpt[Game, Long] = Memoize(db.selectGame)
  val selectScore: MemoizeOpt[Long, (Int, Int)] = Memoize(db.selectScore)
  val selectStat: MemoizeOpt[String, Int] = Memoize(db.selectStat)
  val selectTeam: MemoizeOpt[Team, Int] = Memoize(db.selectTeam)
}
