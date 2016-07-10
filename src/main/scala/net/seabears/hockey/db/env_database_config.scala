package net.seabears.hockey.db

object EnvDatabaseConfig {
  def apply(): DatabaseConfig = {
    val name = sys.env("HOCKEY_DB_NAME")
    val host = sys.env("HOCKEY_DB_HOST")
    val port = sys.env("HOCKEY_DB_PORT").toInt
    val username = sys.env("HOCKEY_DB_USERNAME")
    val password = sys.env("HOCKEY_DB_PASSWORD")
    DatabaseConfig(name, host, port, username, password)
  }
}
