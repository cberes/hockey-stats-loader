package net.seabears.hockey

import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.Element

import net.seabears.hockey.util.DateUtils

object Downloader {
  def apply(destination: String, dateStart: String, dateEnd: String, host: String)(implicit userAgentFactory: () => String, pauseFactory: () => Unit) =
    new Downloader(destination, host, DateUtils.dates(dateStart, dateEnd), userAgentFactory, pauseFactory)
}

class Downloader(destination: String, host: String, dates: Seq[LocalDate], userAgentFactory: () => String, pauseFactory: () => Unit) {
  private[this] val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
  private[this] val userAgent = userAgentFactory()
  private[this] val browser = new JsoupBrowser(userAgent)

  def run() {
    dates.foreach(run)
  }

  def run(date: LocalDate) {
    val dayId = date.format(formatter)
    val urls: List[String] = getUrls(host + dayId)
    urls.zipWithIndex.foreach(url => download(url._1, dayId, url._2))
    pauseFactory()
  }

  private def getUrls(url: String): List[String] = {
    println("Searching for URLs at " + url)
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
    val connection = new URL(url).openConnection()
    connection.setRequestProperty("User-Agent", userAgent)
    val name = "playbyplay-" + dayId + "-" + index + ".html"
    println("Downloading file to " + name)
    val output = new FileOutputStream(new File(destination, name))
    val channel = Channels.newChannel(connection.getInputStream())
    output.getChannel().transferFrom(channel, 0, Long.MaxValue)
    pauseFactory()
  }
}
