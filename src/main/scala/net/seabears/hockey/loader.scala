package net.seabears.hockey

object Loader {
  def apply(scraper: Scraper, adapter: GameAdapter) = new Loader(scraper, adapter)
}

class Loader(scraper: Scraper, adapter: GameAdapter) {
  def run() {
    scraper.getGames.foreach(print) 
  }

  private def print(game: Game) {
    println(game.score)
    Set(game.home, game.away) foreach (team => {
      println(team)
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
