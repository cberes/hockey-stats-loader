package net.seabears.hockey

import net.seabears.hockey.db._

object App {
  def main(args: Array[String]) {
    if (args.head == "download")
      download(args.tail)
    else if (args.head == "schedule")
      schedule(args.tail)
    else if (args.head == "scrape")
      scrape(args.tail)
    else
      throw new IllegalStateException("invalid action");
  }

  private def download(args: Array[String]) {
    Downloader(args(0), args(1), args(2), args(3)).run
  }

  private def schedule(args: Array[String]) {
    val db = new MemoizedDatabase(getDatabase)
    Scheduler(new GameAdapter(_, db), args(0), args(1), args(2)).run
  }

  private def scrape(args: Array[String]) {
    val db = new MemoizedDatabase(getDatabase)
    Loader(new Scraper(args(0)), new GameAdapter(_, db)).run
  }

  private def getDatabase(): Database =
    new AnormDatabase(new DatabaseConnection(EnvDatabaseConfig()))
}
