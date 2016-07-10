package net.seabears.hockey.db

import anorm._

import net.seabears.hockey.core._

class AnormDatabase(db: DatabaseConnection) extends Database {
  def insert(game: Game, homeId: Int, awayId: Int): Long = {
    db.withConnection { implicit c =>
      val id: Option[Long] =
        SQL("insert into game (scheduled, home_team_id, away_team_id) values ({scheduled}, {home}, {away})")
        .on('scheduled -> game.scheduled, 'home -> homeId, 'away -> awayId).executeInsert()
      id.get
    }
  }
  def insert(gameId: Long, homeScore: Int, awayScore: Int) {
    db.withConnection { implicit c =>
      SQL("insert into game_result (game_id, home_score, away_score) values ({game}, {home}, {away})")
      .on('game -> gameId, 'home -> homeScore, 'away -> awayScore).execute()
    }
  }
  def insert(gameId: Long, teamId: Int, statId: Int, value: Double) {
    db.withConnection { implicit c =>
      SQL("insert into game_stat (game_id, team_id, stat_id, value) values ({game}, {team}, {stat}, {value})")
      .on('game -> gameId, 'team -> teamId, 'stat -> statId, 'value -> value).execute()
    }
  }
  val selectGame: Game => Option[Long] = game => {
    val homeId = selectTeam(game.home)
    val awayId = selectTeam(game.away)
    db.withConnection { implicit c =>
      SQL("""
        select _id from game
        where scheduled = {scheduled}
        and home_team_id = {home}
        and away_team_id = {away}
      """)
      .on('scheduled -> game.scheduled, 'home -> homeId, 'away -> awayId).as(SqlParser.long("_id").singleOpt)
    }
  }
  val selectScore: Long => Option[(Int, Int)] = id => {
    db.withConnection { implicit c =>
      val parser: RowParser[(Int, Int)] = SqlParser.int("home_score") ~ SqlParser.int("away_score") map(SqlParser.flatten)
      SQL("select home_score, away_score from game_result where game_id = {game}")
      .on('game -> id).as(parser.singleOpt)
    }
  }
  val selectStat: String => Option[Int] = stat => {
    db.withConnection { implicit c =>
      SQL("select _id from stat where name = {stat}")
      .on('stat -> stat).as(SqlParser.int("_id").singleOpt)
    }
  }
  val selectTeam: Team => Option[Int] = team => {
    db.withConnection { implicit c =>
      SQL("""
        select _id as team_id from team
        where name = {team} or name = {alias}
        union all
        select team_id from team_alias
        where alias = {team} or alias = {alias}
        limit 1
      """)
      .on('team -> team.name, 'alias -> team.alias).as(SqlParser.int("team_id").singleOpt)
    }
  }
}
