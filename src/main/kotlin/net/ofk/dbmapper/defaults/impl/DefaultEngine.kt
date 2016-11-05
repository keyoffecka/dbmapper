package net.ofk.dbmapper.defaults.impl;

import java.sql.Connection

/**
 * Default engine has no its own specific query language variant (dialect)
 * and doesn't need any special preparation of a connection.
 * It also uses a MySQL-like date format.
 */
class DefaultEngine : BaseEngine(DefaultFormatterBuilder.build()) {
  override fun prepareConnection(conn: Connection) {}
  override fun variant() = ""
}
