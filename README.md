# Hockey stats loader

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

    \list
    CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;
    \dx
    \dt

