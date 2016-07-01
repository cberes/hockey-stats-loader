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
  private type TeamEvent = (Team, String)

  private[this] val eventFilters: List[(TeamEvent => Boolean, (Team, Boolean) => GameEvent)] =
    List((eventFilter("goal scored"), GoalScored.apply),
         (eventFilter("shot blocked"), ShotBlocked.apply),
         (eventFilter("shot missed"), ShotMissed.apply),
         (eventFilter("shot on goal"), ShotOnGoal.apply))

  private def eventFilter(search: String)(event: TeamEvent): Boolean =
    event._2.toLowerCase.contains(search)

  def run(): Seq[Game] = {
    new File(dir).listFiles
                 .filter(_.isFile)
                 .filter(f => f.getName.endsWith(".html"))
                 .map(_.getPath)
                 .map(scrape)
  }

  private def scrape(file: String): Game = {
    val browser = JsoupBrowser()
    val doc = browser.parseFile(file)
    val events = getEvents(doc)
    val teamLocations = events.map(_._1).distinct.toSet
    val teamNames = getTeams(doc)
    val teams = (makeTeam(teamNames._1, teamLocations), makeTeam(teamNames._2, teamLocations))
    val game = new Game(teams._1, teams._2, getTime(doc))
    events.map(toTeamEvent(Set(teams._1, teams._2)))
          .map(toGameEvent)
          .filterNot(_.isEmpty)
          .map(_.get)
          .foreach(game.put)
    // TODO remove the printlns
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
    game
  }

  private def getEvents(doc: Document): List[RawEvent] = {
    val rows: List[Element] = doc >> elementList("div.mod-content table.mod-data tbody tr")
    rows.map(_ >> elementList("td"))
	.filter(_.size == 3)
	.map(tds => (tds(1).text, tds{2}.text))
	.filterNot(_._1.isEmpty)
	.filterNot(_._2.isEmpty)
  }

  private def getTeams(doc: Document): (String, String) = {
    val elem: String = doc >> text("title")
    val teams = """^\s*(.+)\s+vs\.\s+(.+)\s+\S\s+Play.*$""".r
    val teams(away, home) = elem
    (home, away)
  }

  // TODO this won't work for teams whose alias is not a substring of their whole name
  private def makeTeam(name: String, aliases: Set[String]): Team =
    Team(name, aliases.filter(name.contains).head)

  private def getTime(doc: Document) = {
    val elem: String = doc >> text(".game-time-location p:first-child")
    val gameTime = """^\s*(\d+:\d+\s+\w+)\s+(\w+)\s*,\s*(.+)\s*$""".r
    val gameTime(rawTime, zone, rawDate) = elem
    val date = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    val time = LocalTime.parse(rawTime, DateTimeFormatter.ofPattern("h:mm a"))
    LocalDateTime.of(date, time)
  }

  private def toTeamEvent(teams: Set[Team])(event: RawEvent): TeamEvent =
    (teams.filter(_.alias == event._1).head, event._2)

  private def toGameEvent(event: TeamEvent): Option[GameEvent] = {
    val evenStrength = !isPowerPlay(event) && !isShorthanded(event)
    (eventFilters filter (_._1(event)) map (_._2(event._1, evenStrength))).headOption
  }

  private def isShorthanded(event: TeamEvent): Boolean =
    event._2.toLowerCase.startsWith("shorthanded")

  private def isPowerPlay(event: TeamEvent): Boolean =
    event._2.toLowerCase.startsWith("power play")
}
