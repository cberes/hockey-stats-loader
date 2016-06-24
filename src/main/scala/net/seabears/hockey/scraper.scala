package net.seabears.hockey

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.Element

object Scraper {
  private type RawEvent = (String, String)

  private[this] val eventFilters: List[(RawEvent => Boolean, (String, Boolean) => GameEvent)] =
    List((eventFilter("goal scored"), GoalScored.apply),
         (eventFilter("shot blocked"), ShotBlocked.apply),
         (eventFilter("shot missed"), ShotMissed.apply),
         (eventFilter("shot on goal"), ShotOnGoal.apply))

  private def eventFilter(search: String)(event: RawEvent): Boolean =
    event._2.toLowerCase.contains(search)

  private def isShorthanded(event: RawEvent): Boolean =
    event._2.toLowerCase.startsWith("shorthanded")

  private def isPowerPlay(event: RawEvent): Boolean =
    event._2.toLowerCase.startsWith("power play")

  private def toGameEvent(event: RawEvent): Option[GameEvent] = {
    val evenStrength = !isPowerPlay(event) && !isShorthanded(event)
    (eventFilters filter (_._1(event)) map (_._2(event._1, evenStrength))).headOption
  }

  def main(args: Array[String]) {
    val browser = JsoupBrowser()
    //val doc = browser.get("http://example.com/")
    val doc = browser.parseFile(getClass.getResource("/playbyplay.html").getPath)
    val rows: List[Element] = doc >> elementList("div.mod-content table.mod-data tbody tr")
    val events: List[RawEvent] = rows.map(_ >> elementList("td"))
	    .filter(_.size == 3)
	    .map(tds => (tds(1).text, tds{2}.text))
	    .filterNot(_._1.isEmpty)
	    .filterNot(_._2.isEmpty)

    val teams = events map (_._1)
    val game = new Game(teams.distinct.toSet)
    events.map(toGameEvent).filterNot(_.isEmpty).map(_.get).foreach(game.put)
    println(game.score)
    game.teams foreach (team => {
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
