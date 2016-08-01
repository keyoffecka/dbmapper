package net.ofk.dbmapper.defaults.impl;

import java.sql.Connection

/**
 * MySQL5 engine has its own variant name
 * and sets UTC time zone for every connection.
 */
class MySQL5Engine : BaseEngine(DefaultFormatterBuilder.build()) {
  override fun prepareConnection(conn: Connection) {
    val st = conn.createStatement()
    try {
      st.execute("set time_zone = '+00:00'")
    } finally {
      st.close()
    }
  }

  override val variant: String = "mysql"
}

