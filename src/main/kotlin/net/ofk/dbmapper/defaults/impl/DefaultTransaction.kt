package net.ofk.dbmapper.defaults.impl

import net.ofk.dbmapper.QueryFactory
import net.ofk.dbmapper.api.TransactionExecutable
import net.ofk.dbmapper.api.Storage
import net.ofk.dbmapper.api.Transaction
import net.ofk.dbmapper.api.TransactionExecutableWithQueries
import net.ofk.dbmapper.defaults.api.Engine
import net.ofk.dbmapper.defaults.api.Session
import net.ofk.kutils.JRE8Utils
import java.sql.Connection
import java.sql.ResultSet

/**
 * Every instance of the default transaction will use one connection per thread.
 * If there is no transaction associated with a thread a new one is created,
 * otherwise the existing one is used.
 * This implementation introduces engines, engines may prepare (if required by an RDBM system) connections
 * before they are actually used and are responsible for creating engine specific queries.
 * It also uses sessions to maintain lifecycles of acquired connections.
 */
class DefaultTransaction(private val session: Session, private val engine: Engine) : Transaction<ResultSet> {
  companion object {
    private val TX = ThreadLocal<Storage<ResultSet>>();
  }

  private val queries = hashMapOf<Class<*>, Any>()
  private val queryFactory = QueryFactory()

  override fun exec(executable: TransactionExecutable<ResultSet>) {
    execute({storage -> executable.execute(storage)})
  }

  override fun <T> execute(executable: (Storage<ResultSet>) -> T): T {
    return doExecute<T, Any>(null, {queries, storage -> executable(storage)})
  }

  override fun <Q> exec(type: Class<Q>, executable: TransactionExecutableWithQueries<Q, ResultSet>) {
    execute(type, { queries, storage -> executable.execute(queries, storage) })
  }

  override fun <T,Q> execute(type: Class<Q>, executable: (queries: Q, Storage<ResultSet>) -> T): T {
    return doExecute(type, {queries, storage -> executable.invoke(queries!!, storage)})
  }

  private fun <T,Q> doExecute(clazz: Class<Q>?, executable: (queries: Q?, Storage<ResultSet>) -> T): T {
    var conn: Connection? = null
    if (TX.get() == null) {
      conn = session.acquire()

      try {
        conn.autoCommit = false
        conn.transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
        engine.prepareConnection(conn)
      } catch (ex: Exception) {
        session.invalidate(conn, ex)
      }

      TX.set(DefaultStorage(conn, engine));
    }

    val result = try {
      executable.invoke(getQueries(clazz), TX.get())
    } catch (ex: Exception) {
      rollback<T>(conn, ex)
    }

    commit(conn)

    return result
  }

  private fun <Q> getQueries(clazz: Class<Q>?): Q? {
    val queries = if (clazz == null) null else {
      synchronized(this.queries) {
        var q = this.queries[clazz] as Q?
        if (q == null) {
          q = queryFactory.create(clazz, engine.variant)
          queries[clazz] = q as Any
        }
        q
      }
    }
    return queries
  }

  private fun commit(conn: Connection?) {
    if (conn != null) {
      try {
        conn.commit()
      } catch(ex: Exception) {
        rollback<Any>(conn, ex)
      }

      try {
        session.release(conn)
      } finally {
        TX.set(null)
      }
    }
  }

  private fun <T> rollback(conn: Connection?, ex: Exception): T {
    if (conn != null) {
      try {
        try {
          conn.rollback()
        } catch(e: Exception) {
          JRE8Utils.INSTANCE.addSuppressed(e, ex)

          session.invalidate(conn, e)
        }

        session.invalidate(conn, ex)
      } finally {
        TX.set(null)
      }
    }

    throw ex
  }
}
