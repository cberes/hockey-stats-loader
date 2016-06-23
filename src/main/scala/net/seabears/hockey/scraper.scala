package net.seabears.hockey

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.Element

object Scraper {
  private def isShorthanded(event: (String, String)): Boolean =
    event._2.toLowerCase.startsWith("shorthanded")

  private def isPowerPlay(event: (String, String)): Boolean =
    event._2.toLowerCase.startsWith("power play")

  private def isShotMissed(event: (String, String)): Boolean =
    event._2.toLowerCase.contains("shot missed")

  private def isShotOnGoal(event: (String, String)): Boolean =
    event._2.toLowerCase.contains("shot on goal")

  private def isShotBlocked(event: (String, String)): Boolean =
    event._2.toLowerCase.contains("shot blocked")

  private def isGoalScored(event: (String, String)): Boolean =
    event._2.toLowerCase.contains("goal scored")

  private def toGameEvent(event: (String, String)): Option[GameEvent] = {
    val evenStrength = !isPowerPlay(event) && !isShorthanded(event)
    if (isShotMissed(event))
      Some(ShotMissed(event._1, evenStrength))
    else if (isShotBlocked(event))
      Some(ShotBlocked(event._1, evenStrength))
    else if (isShotOnGoal(event))
      Some(ShotOnGoal(event._1, evenStrength))
    else if (isGoalScored(event))
      Some(GoalScored(event._1, evenStrength))
    else None
  }

  def main(args: Array[String]) {
    val browser = JsoupBrowser()
    //val doc = browser.get("http://example.com/")
    val doc = browser.parseFile(getClass.getResource("/playbyplay.html").getPath)
    val rows: List[Element] = doc >> elementList("div.mod-content table.mod-data tbody tr")
    val events: List[(String, String)] = rows.map(_ >> elementList("td"))
	    .filter(_.size == 3)
	    .map(tds => (tds(1).text, tds{2}.text))
	    .filterNot(_._1.isEmpty)
	    .filterNot(_._2.isEmpty)

    val teams = events map (_._1) distinct
    val game = new Game(teams.toSet)
    events.map(toGameEvent).filterNot(_.isEmpty).map(_.get).foreach(game.put)
    println(game.score)
    game.teams foreach (team => {
      println(team)
      println(game.corsiClose(team))
      println(game.fenwickClose(team))
    })
  }
}
