package net.seabears.hockey

import net.seabears.hockey.core._

object Loader {
  def apply(scraper: Scraper, adapterFactory: Game => GameAdapter) =
    new Loader(scraper, adapterFactory)
}

class Loader(scraper: Scraper, adapterFactory: Game => GameAdapter) {
  def run() {
    val games = scraper.getGames
    games.map{
      case game: PastGame => Some(game)
      case _ => None
    }.foreach(_.map(print))
    games.map(adapterFactory)
         .filter(_.isNew)
         .foreach(_.save)
  }

  private def print(game: PastGame) {
    println(game.scheduled)
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
