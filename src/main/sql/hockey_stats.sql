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

