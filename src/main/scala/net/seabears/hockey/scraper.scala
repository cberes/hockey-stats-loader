package net.seabears.hockey

import java.io.File
import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneId}
import java.time.format.DateTimeFormatter

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.{Document, Element}

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

  def getGames(): Seq[Game] = {
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
    val game = new PastGame(teams._1, teams._2, getTime(doc))
    computeStats(events, game)
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

  // use longest token for cases where team's alias is not wholly contained in the team's name
  private def makeTeam(name: String, aliases: Set[String]): Team =
    Team(name, aliases.filter(a => name.contains(getLongestToken(a))).head)

  private def getLongestToken(text: String): String =
    text.split("\\s+").maxBy(_.length)

  private def getTime(doc: Document) = {
    val elem: String = doc >> text(".game-time-location p:first-child")
    val gameTime = """^\s*(\d+:\d+\s+\w+)\s+(\w+)\s*,\s*(.+)\s*$""".r
    val gameTime(rawTime, zone, rawDate) = elem
    val date = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    val time = LocalTime.parse(rawTime, DateTimeFormatter.ofPattern("h:mm a"))
    LocalDateTime.of(date, time)
  }

  private def computeStats(events: List[RawEvent], game: PastGame) {
    val teams = Set(game.home, game.away)
    events.map(toTeamEvent(teams))
          .map(toGameEvent)
          .filter(_.isDefined)
          .map(_.get)
          .map(swapShotBlockedTeam(teams))
          .foreach(game.put)
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

  private def swapShotBlockedTeam(teams: Set[Team])(event: GameEvent) = event match {
    case ShotBlocked(team, evenStrength) => ShotBlocked((teams - team).head, evenStrength)
    case other => other
  }
}
