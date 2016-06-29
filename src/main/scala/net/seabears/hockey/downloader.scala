package net.seabears.hockey

import scala.language.postfixOps
import scala.sys.process._
import java.io.File
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.Element

object Downloader {
  def apply(destination: String, dateStart: String, dateEnd: String, host: String) =
    new Downloader(destination, host, dates(dateStart, dateEnd))

  private def dates(dateStart: String, dateEnd: String): Seq[LocalDate] = {
    val start = LocalDate.parse(dateStart)
    val days = ChronoUnit.DAYS.between(start, LocalDate.parse(dateEnd)).toInt
    for (dayIndex <- 0 to days) yield start.plusDays(dayIndex)
  }
}

class Downloader(destination: String, host: String, dates: Seq[LocalDate]) {
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
    println("Searching for URLs at " + url)
    val browser = JsoupBrowser()
    val doc = browser.get(url)
    val links: List[Element] = doc >> elementList(".expand-gameLinks a")
    links.filter(e => e.text.matches("Play.By.Play"))
         .map(e => e.attr("href"))
         .map(_ + "&period=0")
         .map(getFullUrl(url, _))
  }

  private def getFullUrl(sourceUrl: String, path: String) =
    if (path.startsWith("/")) {
      val url = new URL(sourceUrl)
      url.getProtocol + "://" + url.getHost + path
    } else path

  private def download(url: String, dayId: String, index: Int) {
    val name = "playbyplay-" + dayId + "-" + index + ".html"
    println("Downloading file to " + name)
    new URL(url) #> new File(destination, name) !
  }
}
