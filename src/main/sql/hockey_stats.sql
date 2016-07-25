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

CREATE FUNCTION adjust_for_score(in TEXT, in BIGINT, in INT, out value DOUBLE PRECISION)
    AS $$ SELECT (( 3.75 * (COALESCE(s_a2.value, 0.0) - 0.440) +
              8.46 * (COALESCE(s_a1.value, 0.0) - 0.461) +
             17.94 * (COALESCE(s_ev.value, 0.0) - 0.500) +
              8.46 * (COALESCE(s_b1.value, 0.0) - 0.539) +
              3.75 * (COALESCE(s_b2.value, 0.0) - 0.560)
            ) / 42.36
           ) + 0.5
    FROM stat ev --ON ev.name = (CAST($1 AS TEXT) || 'Even')
    LEFT OUTER JOIN game_stat s_ev on s_ev.stat_id = ev._id
      AND s_ev.game_id = $2 AND s_ev.team_id = $3
    JOIN stat a1 ON a1.name = (CAST($1 AS TEXT) || 'Ahead(1)')
    LEFT OUTER JOIN game_stat s_a1 on s_a1.stat_id = ev._id
      AND s_a1.game_id = $2 AND s_a1.team_id = $3
    JOIN stat a2 ON a2.name = (CAST($1 AS TEXT) || 'Ahead(2)')
    LEFT OUTER JOIN game_stat s_a2 on s_a2.stat_id = ev._id
      AND s_a2.game_id = $2 AND s_a2.team_id = $3
    JOIN stat b1 ON b1.name = (CAST($1 AS TEXT) || 'Behind(1)')
    LEFT OUTER JOIN game_stat s_b1 on s_b1.stat_id = ev._id
      AND s_b1.game_id = $2 AND s_b1.team_id = $3
    JOIN stat b2 ON b2.name = (CAST($1 AS TEXT) || 'Behind(2)')
    LEFT OUTER JOIN game_stat s_b2 on s_b2.stat_id = ev._id
      AND s_b2.game_id = $2 AND s_b2.team_id = $3
    WHERE ev.name = (CAST($1 AS TEXT) || 'Even')
    $$ LANGUAGE SQL;

CREATE FUNCTION avg_stat(in TEXT, in INT, in TIMESTAMP, in TIMESTAMP, out value DOUBLE PRECISION)
    AS $$ SELECT avg(adjust_for_score($1, g._id, t._id))
    FROM team t
    JOIN game g ON g.home_team_id = t._id OR g.away_team_id = t._id
    JOIN game_result r ON r.game_id = g._id
    WHERE t._id = $2 AND g.scheduled >= $3 AND g.scheduled < $4
    $$ LANGUAGE SQL;

CREATE OR REPLACE VIEW predictions AS
    SELECT g._id as game_id
          ,CAST(5.3 * avg_stat('Corsi%', h._id, g.scheduled - interval '2 months', g.scheduled) AS INT) AS home_score
          ,CAST(5.3 * avg_stat('Corsi%', a._id, g.scheduled - interval '2 months', g.scheduled) AS INT) AS away_score
    FROM game g
    JOIN team h ON g.home_team_id = h._id
    JOIN team a ON g.away_team_id = a._id;

-- training data
SELECT avg_stat('Corsi%', g.home_team_id, g.scheduled - interval '2 months', g.scheduled) as home_stat
      ,avg_stat('Corsi%', g.away_team_id, g.scheduled - interval '2 months', g.scheduled) as away_stat
      ,r.home_score
      ,r.away_score
FROM game g
JOIN game_result r ON g._id = r.game_id
LIMIT 10000;

