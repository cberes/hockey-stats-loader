package net.seabears.hockey

import java.sql.DriverManager
import java.sql.Connection

/*
 * Adapted from http://bwbecker.github.io/blog/2015/05/05/accessing-anorm-without-play/
 * which was also "adapted" from Play
 */
object DB {
  private[this] val host = "TODO"
  private[this] val port = 5432
  private[this] val name = "TODO"
  private[this] val url = s"jdbc:postgresql://$host:$port/$name"
  private[this] val user = "TODO"
  private[this] val password = "TODO"

  Class.forName("org.postgresql.Driver").newInstance

  /**
   * Retrieves a JDBC connection.
   *
   * Don't forget to release the connection at some point by calling close().
   *
   * @return a JDBC connection
   * @throws an error if the required data source is not registered
   */
  def getConnection(): Connection = {
    var props = new java.util.Properties();
    props.setProperty("user", user);
    props.setProperty("password", password);

    DriverManager.getConnection(url, props)
  }

  /**
   * Retrieves a JDBC connection.
   *
   * Don't forget to release the connection at some point by calling close().
   *
   * @param autocommit when `true`, sets this connection to auto-commit
   * @return a JDBC connection
   * @throws an error if the required data source is not registered
   */
  def getConnection(autocommit: Boolean = true): Connection = {
    val connection = this.getConnection
    connection.setAutoCommit(autocommit)
    connection
  }

  /**
   * Execute a block of code, providing a JDBC connection. The connection and all created statements are
   * automatically released.
   *
   * @param name The datasource name.
   * @param block Code block to execute.
   */
  def withConnection[A](block: Connection ⇒ A): A = {
    val connection = getConnection
    try {
      block(connection)
    } finally {
      connection.close()
    }
  }
}
