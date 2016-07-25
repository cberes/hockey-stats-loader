# Hockey stats loader

Scrapes hockey stats from HTML markup

## Usage

First, download files containing raw stats to parse (in HTML):

    sbt run download /path/to/save/files 2016-06-01 2016-06-01 http://example.com/games?date=

Then parse the stats files in the same directory:

    sbt run scrape /path/to/read/files

Alternatively, parse HTML containing scheduled games:

    sbt run schedule 2016-06-01 2016-06-01 http://example.com/games?date=

## Configuration

The following environment variables need to be set:

0. `HOCKEY_DB_NAME` (database name)
0. `HOCKEY_DB_HOST` (database host)
0. `HOCKEY_DB_PORT` (database port)
0. `HOCKEY_DB_USERNAME` (database username)
0. `HOCKEY_DB_PASSWORD` (database password)

## Notes

### Postgres Docker container

#### Start

    docker run --name hockey-stats-db \
      -e POSTGRES_PASSWORD=abc123 \
      -e POSTGRES_DB=hockey_stats \
      -e PGDATA=/var/lib/postgresql/data/hockey \
      -d postgres

#### Connect

    docker run -it --rm --link hockey-stats-db:postgres postgres \
      psql -h postgres -U postgres -d hockey_stats

#### Setup

0. Run the [schema file](src/main/sql/hockey_stats.sql)
0. Add your teams and their aliases

