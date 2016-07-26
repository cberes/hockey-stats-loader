package net.seabears.hockey

import java.time.{LocalDate, LocalTime, ZonedDateTime}
import java.time.format.DateTimeFormatter

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.Element

import net.seabears.hockey.core._
import net.seabears.hockey.util.DateUtils

object Scheduler {
  def apply(adapterFactory: Game => GameAdapter, dateStart: String, dateEnd: String, host: String)(implicit userAgentFactory: () => String) =
    new Scheduler(adapterFactory, host, DateUtils.dates(dateStart, dateEnd), userAgentFactory)
}

class Scheduler(adapterFactory: Game => GameAdapter, host: String, dates: Seq[LocalDate], userAgentFactory: () => String) {
  private[this] val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
  private[this] val browser = new JsoupBrowser(userAgentFactory())

  def run() {
    dates.flatMap(getGames)
         .map(adapterFactory)
         .filter(_.isNew)
         .foreach(_.save)
  }

  private[this] def getGames(date: LocalDate): List[FutureGame] = {
    val dayId = date.format(formatter)
    val url = host + dayId
    println("Searching for games at " + url)
    val doc = browser.get(url)
    val tables: List[Element] = doc >> elementList("div.game-header")
    tables.map(toGame(date))
  }

  private def toGame(date: LocalDate)(element: Element): FutureGame = {
    val away = element.select("table.game-header-table tr:nth-child(1) td.team-name").head.text
    val home = element.select("table.game-header-table tr:nth-child(3) td.team-name").head.text
    val time = element.select("ul.game-info li:nth-child(2) span:first-child").head.text
    FutureGame(Team("", home), Team("", away), parseTime(date, time))
  }

  private def parseTime(date: LocalDate, timeToParse: String) = {
    val gameTime = """^\s*(\d+:\d+\s+\w+)\s+(\w+)\s*$""".r
    val gameTime(rawTime, rawZone) = timeToParse
    val time = LocalTime.parse(rawTime, DateTimeFormatter.ofPattern("h:mm a"))
    ZonedDateTime.of(date, time, DateUtils.parseZone(rawZone))
  }
}
