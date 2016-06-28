package net.seabears.hockey

import scala.sys.process._
import java.io.File
import java.net.URL
import java.time.{Duration, LocalDate}
import java.time.format.DateTimeFormatter

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.Element

object Downloader {
  def apply(host: String, destination: String, dateStart: String, dateEnd: String) =
    new Downloader(host, destination, dates(dateStart, dateEnd))

  private def dates(dateStart: String, dateEnd: String): Seq[LocalDate] = {
    val start = LocalDate.parse(dateStart)
    val days = Duration.between(start, LocalDate.parse(dateEnd)).toDays
    for (dayIndex <- 0 to days) yield start.plusDays(dayIndex)
  }
}

class Downloader(host: String, destination: String, dates: Seq[LocalDate]) {
  private[this] val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

  def run() {
    dates.foreach(run)
  }

  def run(date: LocalDate) {
    val dayId = date.format(formatter)
    val urls: List[String] = getUrls(host + dayId)
    urls.zipWithIndex.map(url => download(url._1, dayId, url._2))
  }

  private def getUrls(url: String): List[String] = {
    val doc = browser.get(host + dayId)
    val links: List[Element] = doc >> elementList(".expand-gameLinks a")
    links.filter(e => e.text == "Play-By-Play").map(e => e.attr("href"))
  }

  private def download(url: String, dayId: String, index: Int) {
    val name = "playbyplay-" + dayId + "-" + index + ".html"
    new Url(url) #> new File(destination, name) !
  }
}
