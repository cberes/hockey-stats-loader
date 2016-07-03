package net.seabears.hockey

class GameAdapter(val game: Game, db: Database) {
  val homeTeamId: Int = findTeam(game.home)
  val awayTeamId: Int = findTeam(game.away)
  var gameId: Int = 0

  private def findTeam(team: Team): Int = db.selectTeam(team)

  // TODO new if FutureGame and no record exists OR PastGame and score does not exist
  def isNew(): Boolean = true

  def save(): Unit = game match {
    case futureGame: FutureGame => saveScheduledGame(futureGame)
    case pastGame: PastGame => saveFinalGame(pastGame)
  }

  private def saveScheduledGame(game: Game) {
    gameId = db.insert(game)
  }

  private def saveFinalGame(game: PastGame) {
    // TODO insert game only if base game record does not exist
    saveScheduledGame(game)
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
