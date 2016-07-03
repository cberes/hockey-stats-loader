package net.seabears.hockey

class GameAdapter(val game: Game, db: Database) {
  val homeTeamId: Int = findTeam(game.home)
  val awayTeamId: Int = findTeam(game.away)
  var gameId: Int = 0

  private def findTeam(team: Team): Int = db.selectTeam(team)

  def isDuplicate(): Boolean = false

  def save() {
    gameId = db.insert(game)
    // TODO do not insert result/stats for future games
    // TODO what can I check on a game to know it has no stats?
    db.insert(gameId, game.score(game.home), game.score(game.away))
    teams.foreach(team => {
      game.corsiPctAll(team).foreach{case (bucket, value) => {
        db.insert(gameId, findTeam(team), db.selectStat("Corsi", bucket), value)
      }}
      game.fenwickPctAll(team).foreach{case (bucket, value) => {
        db.insert(gameId, findTeam(team), db.selectStat("Fenwick", bucket), value)
      }}
      game.rawStats(team).foreach{case (bucket, stats) => {
        stats.foreach{case (stat, value) => {
          db.insert(gameId, findTeam(team), db.selectStat(stat, bucket), value)
        }}
      }}
    })
  }

  private def teams() = Set(game.home, game.away)
}
