package net.seabears.hockey

class GameAdapter(val game: Game, db: Database) {
  val homeTeamId: Int = findTeam(game.home)
  val awayTeamId: Int = findTeam(game.away)
  var gameId: Option[Int] = db.selectGame(game)

  private def findTeam(team: Team): Int = db.selectTeam(team)

  def isNew(): Boolean = gameId match {
    case Some(id) => game.isInstanceOf[PastGame] && db.selectScore(id).isEmpty
    case None => true
  }

  def save(): Unit = game match {
    case futureGame: FutureGame => saveScheduledGame(futureGame)
    case pastGame: PastGame => saveFinalGame(pastGame)
  }

  private def saveScheduledGame(game: Game) {
    gameId = Some(db.insert(game))
  }

  private def saveFinalGame(game: PastGame) {
    if (gameId.isEmpty) saveScheduledGame(game)
    db.insert(gameId.get, game.score(game.home), game.score(game.away))
    teams.foreach(team => {
      game.corsiPctAll(team).foreach{case (bucket, value) => {
        db.insert(gameId.get, findTeam(team), db.selectStat("Corsi", bucket), value)
      }}
      game.fenwickPctAll(team).foreach{case (bucket, value) => {
        db.insert(gameId.get, findTeam(team), db.selectStat("Fenwick", bucket), value)
      }}
      game.rawStats(team).foreach{case (bucket, stats) => {
        stats.foreach{case (stat, value) => {
          db.insert(gameId.get, findTeam(team), db.selectStat(stat, bucket), value)
        }}
      }}
    })
  }

  private def teams() = Set(game.home, game.away)
}
