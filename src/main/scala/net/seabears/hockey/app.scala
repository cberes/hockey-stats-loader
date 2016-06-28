package net.seabears.hockey

object App {
  def main(args: Array[String]) {
    if (args.head == "download")
      download(args.tail)
    else if (args.head == "scrape")
      scrape(args.tail)
    else
      throw new IllegalStateException("invalid action");
  }

  private def download(args: Array[String]) {
    Downloader(args(0), args(1), args(2), args(3)).run
  }

  private def scrape(args: Array[String]) {
    Scraper(args(0)).run
  }
}
