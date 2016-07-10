# Hockey stats loader

Scrapes hockey stats from HTML markup

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

