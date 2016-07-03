package net.seabears.hockey

object Loader {
  def apply(scraper: Scraper, adapterFactory: Game => GameAdapter) =
    new Loader(scraper, adapterFactory)
}

class Loader(scraper: Scraper, adapterFactory: Game => GameAdapter) {
  def run() {
    scraper.getGames.foreach(print)
    scraper.getGames
           .map(adapterFactory)
           .filterNot(_.isDuplicate)
           .foreach(_.save)
  }

  private def print(game: Game) {
    println(game.score)
    Set(game.home, game.away) foreach (team => {
      println(team)
      println(game.rawStats(team))
      println(game.corsi(Close)(team))
      println(game.corsiPct(Close)(team))
      println(game.corsiAll(team))
      println(game.corsiPctAll(team))
      println(game.fenwick(Close)(team))
      println(game.fenwickPct(Close)(team))
      println(game.fenwickAll(team))
      println(game.fenwickPctAll(team))
    })
  }
}