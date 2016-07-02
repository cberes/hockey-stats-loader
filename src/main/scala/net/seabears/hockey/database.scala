package net.seabears.hockey

class Database {
  def insert(game: Game): Int = 0
  def insert(gameId: Int, homeScore: Int, awayScore: Int) {}
  def insert(gameId: Int, teamId: Int, statId: Int, value: Double) {}
  def selectStat(prefix: String, bucket: Bucket): Int = 0
  def selectTeam(team: Team): Int = 0
}
