CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;

CREATE TABLE IF NOT EXISTS public.stat (
_id SERIAL PRIMARY KEY
,name citext NOT NULL UNIQUE
,description citext NOT NULL
);

CREATE TABLE IF NOT EXISTS public.team (
_id SERIAL PRIMARY KEY
,name citext NOT NULL UNIQUE
,location citext NOT NULL
);

CREATE TABLE IF NOT EXISTS public.team_alias (
team_id SERIAL references team(_id)
,alias citext NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS public.game (
_id BIGSERIAL PRIMARY KEY
,scheduled timestamp NOT NULL
,home_team_id SERIAL references team(_id)
,away_team_id SERIAL references team(_id)
);

CREATE TABLE IF NOT EXISTS public.game_result (
game_id BIGSERIAL references game(_id)
,home_score integer NOT NULL CHECK(home_score >= 0)
,away_score integer NOT NULL CHECK(away_score >= 0)
);

CREATE TABLE IF NOT EXISTS public.game_stat (
game_id BIGSERIAL references game(_id)
,team_id SERIAL references team(_id)
,stat_id SERIAL references stat(_id)
,value real NOT NULL
);

INSERT INTO public.stat (name, description) VALUES
('ShotOnGoalEven', 'Shots on goal for - even'),
('ShotOnGoalClose', 'Shots on goal for - close'),
('ShotOnGoalAhead(1)', 'Shots on goal for - ahead 1'),
('ShotOnGoalAhead(2)', 'Shots on goal for - ahead 2'),
('ShotOnGoalAhead(3)', 'Shots on goal for - ahead 3+'),
('ShotOnGoalBehind(1)', 'Shots on goal for - behind 1'),
('ShotOnGoalBehind(2)', 'Shots on goal for - behind 2'),
('ShotOnGoalBehind(3)', 'Shots on goal for - behind 3+'),
('ShotMissedEven', 'Missed shots for - even'),
('ShotMissedClose', 'Missed shots for - close'),
('ShotMissedAhead(1)', 'Missed shots for - ahead 1'),
('ShotMissedAhead(2)', 'Missed shots for - ahead 2'),
('ShotMissedAhead(3)', 'Missed shots for - ahead 3+'),
('ShotMissedBehind(1)', 'Missed shots for - behind 1'),
('ShotMissedBehind(2)', 'Missed shots for - behind 2'),
('ShotMissedBehind(3)', 'Missed shots for - behind 3+'),
('ShotBlockedEven', 'Blocked shots for - even'),
('ShotBlockedClose', 'Blocked shots for - close'),
('ShotBlockedAhead(1)', 'Blocked shots for - ahead 1'),
('ShotBlockedAhead(2)', 'Blocked shots for - ahead 2'),
('ShotBlockedAhead(3)', 'Blocked shots for - ahead 3+'),
('ShotBlockedBehind(1)', 'Blocked shots for - behind 1'),
('ShotBlockedBehind(2)', 'Blocked shots for - behind 2'),
('ShotBlockedBehind(3)', 'Blocked shots for - behind 3+'),
('CorsiEven', 'Corsi for - even'),
('CorsiClose', 'Corsi for - close'),
('CorsiAhead(1)', 'Corsi for - ahead 1'),
('CorsiAhead(2)', 'Corsi for - ahead 2'),
('CorsiAhead(3)', 'Corsi for - ahead 3+'),
('CorsiBehind(1)', 'Corsi for - behind 1'),
('CorsiBehind(2)', 'Corsi for - behind 2'),
('CorsiBehind(3)', 'Corsi for - behind 3+'),
('Corsi%Even', 'Corsi for % - even'),
('Corsi%Close', 'Corsi for % - close'),
('Corsi%Ahead(1)', 'Corsi for % - ahead 1'),
('Corsi%Ahead(2)', 'Corsi for % - ahead 2'),
('Corsi%Ahead(3)', 'Corsi for % - ahead 3+'),
('Corsi%Behind(1)', 'Corsi for % - behind 1'),
('Corsi%Behind(2)', 'Corsi for % - behind 2'),
('Corsi%Behind(3)', 'Corsi for % - behind 3+'),
('FenwickEven', 'Fenwick for - even'),
('FenwickClose', 'Fenwick for - close'),
('FenwickAhead(1)', 'Fenwick for - ahead 1'),
('FenwickAhead(2)', 'Fenwick for - ahead 2'),
('FenwickAhead(3)', 'Fenwick for - ahead 3+'),
('FenwickBehind(1)', 'Fenwick for - behind 1'),
('FenwickBehind(2)', 'Fenwick for - behind 2'),
('FenwickBehind(3)', 'Fenwick for - behind 3+'),
('Fenwick%Even', 'Fenwick for % - even'),
('Fenwick%Close', 'Fenwick for % - close'),
('Fenwick%Ahead(1)', 'Fenwick for % - ahead 1'),
('Fenwick%Ahead(2)', 'Fenwick for % - ahead 2'),
('Fenwick%Ahead(3)', 'Fenwick for % - ahead 3+'),
('Fenwick%Behind(1)', 'Fenwick for % - behind 1'),
('Fenwick%Behind(2)', 'Fenwick for % - behind 2'),
('Fenwick%Behind(3)', 'Fenwick for % - behind 3+');

-- SA Corsi% by game and team
SELECT g._id, t._id,
(( 3.75 * (COALESCE(s_a2.value, 0.0) - 0.440) +
   8.46 * (COALESCE(s_a1.value, 0.0) - 0.461) +
  17.94 * (COALESCE(s_ev.value, 0.0) - 0.500) +
   8.46 * (COALESCE(s_b1.value, 0.0) - 0.539) +
   3.75 * (COALESCE(s_b2.value, 0.0) - 0.560)
  ) / 42.36
) + 0.5
FROM game g
JOIN team t ON g.home_team_id = t._id OR g.away_team_id = t._id
JOIN stat ev ON ev.name = 'Corsi%Even'
JOIN stat a1 ON a1.name = 'Corsi%Ahead(1)'
JOIN stat a2 ON a2.name = 'Corsi%Ahead(2)'
JOIN stat b1 ON b1.name = 'Corsi%Behind(1)'
JOIN stat b2 ON b2.name = 'Corsi%Behind(2)'
LEFT OUTER JOIN game_stat s_ev ON s_ev.stat_id = ev._id AND s_ev.game_id = g._id AND s_ev.team_id = t._id
LEFT OUTER JOIN game_stat s_a1 ON s_a1.stat_id = a1._id AND s_a1.game_id = g._id AND s_a1.team_id = t._id
LEFT OUTER JOIN game_stat s_a2 ON s_a2.stat_id = a2._id AND s_a2.game_id = g._id AND s_a2.team_id = t._id
LEFT OUTER JOIN game_stat s_b1 ON s_b1.stat_id = b1._id AND s_b1.game_id = g._id AND s_b1.team_id = t._id
LEFT OUTER JOIN game_stat s_b2 ON s_b2.stat_id = b2._id AND s_b2.game_id = g._id AND s_b2.team_id = t._id
group by g._id, t._id, s_ev.value, s_a1.value, s_a2.value, s_b1.value, s_b2.value

-- Past SA Corsi% by game and team
CREATE OR REPLACE VIEW past_corsi_rel AS
SELECT gg._id AS game_id, t._id AS team_id, avg(
(( 3.75 * (COALESCE(s_a2.value, 0.0) - 0.440) +
   8.46 * (COALESCE(s_a1.value, 0.0) - 0.461) +
  17.94 * (COALESCE(s_ev.value, 0.0) - 0.500) +
   8.46 * (COALESCE(s_b1.value, 0.0) - 0.539) +
   3.75 * (COALESCE(s_b2.value, 0.0) - 0.560)
  ) / 42.36
) + 0.5) AS corsi_rel
FROM game gg
JOIN team t ON gg.home_team_id = t._id OR gg.away_team_id = t._id
JOIN stat ev ON ev.name = 'Corsi%Even'
JOIN stat a1 ON a1.name = 'Corsi%Ahead(1)'
JOIN stat a2 ON a2.name = 'Corsi%Ahead(2)'
JOIN stat b1 ON b1.name = 'Corsi%Behind(1)'
JOIN stat b2 ON b2.name = 'Corsi%Behind(2)'
JOIN (SELECT g._id AS game_id, t._id AS team_id,
gg._id AS prior_game_id,
dense_rank() OVER (PARTITION BY g._id, t._id ORDER BY gg.scheduled desc) AS games_prior
FROM game g
JOIN team t ON g.home_team_id = t._id OR g.away_team_id = t._id
JOIN game gg ON (gg.home_team_id = t._id OR gg.away_team_id = t._id) AND gg.scheduled < g.scheduled
GROUP BY g._id, t._id, gg._id) b ON gg._id = b.game_id AND t._id = b.team_id
JOIN game g ON g._id = b.prior_game_id AND b.games_prior <= 20
JOIN game_result r ON r.game_id = g._id
LEFT OUTER JOIN game_stat s_ev ON s_ev.stat_id = ev._id AND s_ev.game_id = g._id AND s_ev.team_id = t._id
LEFT OUTER JOIN game_stat s_a1 ON s_a1.stat_id = a1._id AND s_a1.game_id = g._id AND s_a1.team_id = t._id
LEFT OUTER JOIN game_stat s_a2 ON s_a2.stat_id = a2._id AND s_a2.game_id = g._id AND s_a2.team_id = t._id
LEFT OUTER JOIN game_stat s_b1 ON s_b1.stat_id = b1._id AND s_b1.game_id = g._id AND s_b1.team_id = t._id
LEFT OUTER JOIN game_stat s_b2 ON s_b2.stat_id = b2._id AND s_b2.game_id = g._id AND s_b2.team_id = t._id
GROUP BY gg._id, t._id;

