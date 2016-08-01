package net.ofk.dbmapper.defaults.impl

import net.ofk.dbmapper.defaults.api.Factory
import java.sql.Connection
import java.sql.DriverManager

/**
 * Default connection factory uses a connection string
 * with optional user name and password to create connections.
 */
class DefaultFactory(
  private val url: String,
  private val userName: String?,
  private val password: String?
) : Factory {
  override fun create(): Connection
    = DriverManager.getConnection(url, userName, password)
}

