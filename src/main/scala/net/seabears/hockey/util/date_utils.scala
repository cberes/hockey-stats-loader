package net.seabears.hockey.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object DateUtils {
  def dates(dateStart: String, dateEnd: String): Seq[LocalDate] = {
    val start = LocalDate.parse(dateStart)
    val days = ChronoUnit.DAYS.between(start, LocalDate.parse(dateEnd)).toInt
    for (dayIndex <- 0 to days) yield start.plusDays(dayIndex)
  }

  def parseZone(rawZone: String): ZoneId = {
    ZoneId.of(rawZone match {
      case "ET" => "America/New_York"
      case unknown => unknown
    })
  }
}

