package net.seabears.hockey

import java.io.File
import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneId}
import java.time.format.DateTimeFormatter

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.{Document, Element}

object Scraper {
  def apply(source: String) = new Scraper(source)
}

class Scraper(dir: String) {
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

  def run() {
    new File(dir).listFiles
                 .filter(_.isFile)
                 .filter(f => f.getName.endsWith(".html"))
                 .map(_.getPath)
                 .foreach(scrape)
  }

  private def scrape(file: String) {
    val browser = JsoupBrowser()
    val doc = browser.parseFile(file)
    println(getTeams(doc))
    println(getTime(doc))
    val events = getEvents(doc)

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

  private def getTeams(doc: Document): (String, String) = {
    val elem: String = doc >> text("title")
    val teams = """^\s*(.+)\s+vs\.\s+(.+)\s+\S\s+Play.*$""".r
    val teams(away, home) = elem
    (home, away)
  }

  private def getTime(doc: Document) = {
    val elem: String = doc >> text(".game-time-location p:first-child")
    val gameTime = """^\s*(\d+:\d+\s+\w+)\s+(\w+)\s*,\s*(.+)\s*$""".r
    val gameTime(rawTime, zone, rawDate) = elem
    val date = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    val time = LocalTime.parse(rawTime, DateTimeFormatter.ofPattern("h:mm a"))
    LocalDateTime.of(date, time)
  }

  private def getEvents(doc: Document): List[RawEvent] = {
    val rows: List[Element] = doc >> elementList("div.mod-content table.mod-data tbody tr")
    rows.map(_ >> elementList("td"))
	.filter(_.size == 3)
	.map(tds => (tds(1).text, tds{2}.text))
	.filterNot(_._1.isEmpty)
	.filterNot(_._2.isEmpty)
  }
}
