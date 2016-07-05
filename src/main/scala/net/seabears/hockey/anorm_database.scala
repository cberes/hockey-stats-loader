package net.seabears.hockey

import anorm._

class AnormDatabase extends Database {
  def insert(game: Game, homeId: Int, awayId: Int): Long = {
    DB.withConnection { implicit c =>
      val id: Option[Long] =
        SQL("insert game (scheduled, home_team_id, away_team_id) values ({scheduled}, {home}, {away})")
        .on('scheduled -> game.scheduled, 'home -> homeId, 'away -> awayId).executeInsert()
      id.get
    }
  }
  def insert(gameId: Long, homeScore: Int, awayScore: Int) {
    DB.withConnection { implicit c =>
      SQL("insert game_result (game_id, home_score, away_score) values ({game}, {home}, {away})")
      .on('game -> gameId, 'home -> homeScore, 'away -> awayScore).execute()
    }
  }
  def insert(gameId: Long, teamId: Int, statId: Int, value: Double) {
    DB.withConnection { implicit c =>
      SQL("insert game_star (game_id, team_id, stat_id, value) values ({game}, {team}, {stat}, {value})")
      .on('game -> gameId, 'team -> teamId, 'stat -> statId, 'value -> value).execute()
    }
  }
  val selectGame: Game => Option[Long] = game => {
    val homeId = selectTeam(game.home)
    val awayId = selectTeam(game.away)
    DB.withConnection { implicit c =>
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
    DB.withConnection { implicit c =>
      val parser: RowParser[(Int, Int)] = SqlParser.int("home_score") ~ SqlParser.int("away_score") map(SqlParser.flatten)
      SQL("select home_score, away_score from game_result where _id = {game}")
      .on('game -> id).as(parser.singleOpt)
    }
  }
  val selectStat: String => Option[Int] = stat => {
    DB.withConnection { implicit c =>
      SQL("select _id from stat where name = {name}")
      .on('name -> stat).as(SqlParser.int("_id").singleOpt)
    }
  }
  val selectTeam: Team => Option[Int] = team => {
    DB.withConnection { implicit c =>
      SQL("""
        select _id as team_id from team
        where name = {team} or name = {alias}
        union all
        select team_id from team_alias
        where alias = {team} or alias = {alias}
      """")
      .on('name -> team.name, 'alias -> team.alias).as(SqlParser.int("team_id").singleOpt)
    }
  }
}
