import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.Element

trait GameEvent
case class ShotBlocked(team: String) extends GameEvent
case class ShotMissed(team: String) extends GameEvent
case class ShotOnGoal(team: String) extends GameEvent
case class GoalScored(team: String) extends GameEvent

object Scraper {
  private[this] def isShorthanded(event: (String, String)): Boolean =
    event._2.toLowerCase.startsWith("shorthanded")

  private[this] def isPowerPlay(event: (String, String)): Boolean =
    event._2.toLowerCase.startsWith("power play")

  private[this] def isShotMissed(event: (String, String)): Boolean =
    event._2.toLowerCase.startsWith("shot missed")

  private[this] def isShotOnGoal(event: (String, String)): Boolean =
    event._2.toLowerCase.startsWith("shot on goal")

  private[this] def isShotBlocked(event: (String, String)): Boolean =
    event._2.toLowerCase.contains("shot blocked")

  private[this] def isGoalScored(event: (String, String)): Boolean =
    event._2.toLowerCase.contains("goal scored")

  private[this] def toGameEvent(event: (String, String)): Option[GameEvent] = {
    if (isShotMissed(event))
      Some(new ShotMissed(event._1))
    else if (isShotBlocked(event))
      Some(new ShotBlocked(event._1))
    else if (isShotOnGoal(event))
      Some(new ShotOnGoal(event._1))
    else if (isGoalScored(event))
      Some(new GoalScored(event._1))
    else None
  }

  def main(args: Array[String]) {
    val browser = JsoupBrowser()
    //val doc = browser.get("http://example.com/")
    val doc = browser.parseFile(getClass.getResource("/playbyplay.html").getPath)
    val rows: List[Element] = doc >> elementList("div.mod-content table.mod-data tbody tr")
    val events: List[(String, String)] = rows.map(_ >> elementList("td"))
	    .filter(_.size == 3)
	    .map(tds => (tds(1).text, tds{2}.text))
	    .filterNot(_._1.isEmpty)
	    .filterNot(_._2.isEmpty)
    val evenStrengthEvents: List[(String, String)] = events filterNot isShorthanded filterNot isPowerPlay
    evenStrengthEvents filter isShotMissed foreach println
    evenStrengthEvents filter isShotBlocked foreach println
    evenStrengthEvents filter isShotOnGoal foreach println
    evenStrengthEvents filter isGoalScored foreach println
    //evenStrengthEvents map toGameEvent filterNot _.isEmpty foreach println
    evenStrengthEvents.map(toGameEvent).filterNot(_.isEmpty).map(_.get).foreach(println)
  }
}
