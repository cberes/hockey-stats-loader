package net.seabears.hockey.util

import scala.concurrent.duration._
import scala.util.Random

class PauseFactory(lower: Int, upper: Int) extends (() => Unit) {
  def apply(): Unit = {
    val time = Random.nextInt(upper - lower) + lower
    if (time > 0) Thread.sleep(Duration(time, SECONDS).toMillis)
  }
}
