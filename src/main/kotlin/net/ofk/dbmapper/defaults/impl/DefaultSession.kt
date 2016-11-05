package net.ofk.dbmapper.defaults.impl

import net.ofk.dbmapper.defaults.api.Factory
import net.ofk.dbmapper.defaults.api.Session
import net.ofk.kutils.JRE8Utils
import java.sql.Connection

/**
 * Default connection session creates new connections for every #acquire call.
 */
class DefaultSession(
  private val factory: Factory
) : Session {
  private val connections = arrayListOf<Connection>()

  override fun acquire(): Connection {
    return synchronized(connections) {
      val connection = factory.create();
      this.connections.add(connection);
      connection
    }
  }

  override fun release(conn: Connection) {
    synchronized(connections) {
      if (!connections.remove(conn)) {
        throw IllegalStateException()
      }
    }
    conn.close()
  }

  override fun invalidate(conn: Connection, prev: Exception) {
    try {
      this.release(conn)
    } catch (ex: Exception) {
      JRE8Utils.INSTANCE.addSuppressed(ex, prev)
      throw ex
    }

    throw prev
  }
}