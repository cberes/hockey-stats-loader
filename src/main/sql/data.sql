-- spread (score difference)
\copy (SELECT r.home_score - r.away_score as score_diff,d1.corsi_rel as home_corsi_rel,d2.corsi_rel as away_corsi_rel FROM game g JOIN game_result r ON r.game_id = g._id JOIN past_corsi_rel d1 ON g._id = d1.game_id AND g.home_team_id = d1.team_id JOIN past_corsi_rel d2 ON g._id = d2.game_id AND g.away_team_id = d2.team_id WHERE g.scheduled >= '2015-11-01') to '/home/corey/hockey_data.csv' with CSV HEADER

-- over/under (score total)
\copy (SELECT r.home_score + r.away_score as score_sum,d1.corsi_rel as home_corsi_rel,d2.corsi_rel as away_corsi_rel FROM game g JOIN game_result r ON r.game_id = g._id JOIN past_corsi_rel d1 ON g._id = d1.game_id AND g.home_team_id = d1.team_id JOIN past_corsi_rel d2 ON g._id = d2.game_id AND g.away_team_id = d2.team_id WHERE g.scheduled >= '2015-11-01') to '/home/corey/hockey_data_ou.csv' with CSV HEADER

-- home score
\copy (SELECT r.home_score as home_score,d1.corsi_rel as home_corsi_rel,d2.corsi_rel as away_corsi_rel FROM game g JOIN game_result r ON r.game_id = g._id JOIN past_corsi_rel d1 ON g._id = d1.game_id AND g.home_team_id = d1.team_id JOIN past_corsi_rel d2 ON g._id = d2.game_id AND g.away_team_id = d2.team_id WHERE g.scheduled >= '2015-11-01') to '/home/corey/hockey_data_home.csv' with CSV HEADER

-- away score
\copy (SELECT r.away_score as away_score,d1.corsi_rel as home_corsi_rel,d2.corsi_rel as away_corsi_rel FROM game g JOIN game_result r ON r.game_id = g._id JOIN past_corsi_rel d1 ON g._id = d1.game_id AND g.home_team_id = d1.team_id JOIN past_corsi_rel d2 ON g._id = d2.game_id AND g.away_team_id = d2.team_id WHERE g.scheduled >= '2015-11-01') to '/home/corey/hockey_data_away.csv' with CSV HEADER

