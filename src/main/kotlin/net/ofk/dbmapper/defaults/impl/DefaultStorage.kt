package net.ofk.dbmapper.defaults.impl

import net.ofk.dbmapper.api.Mapper
import net.ofk.dbmapper.api.Storage
import net.ofk.dbmapper.defaults.api.Engine
import net.ofk.kutils.Auto
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

/**
 * An implementation of the storage which is needed only and created by the default transaction implementation.
 */
internal class DefaultStorage(
  private val conn: Connection,
  private val engine: Engine
): Storage<ResultSet> {
  companion object {
     private val LOG = LoggerFactory.getLogger(DefaultStorage::class.java)
  }

  override fun update(queryTemplate: String, vararg paramValues: Any?): Int {
    val query = engine.buildQuery(queryTemplate, *paramValues)

    return Auto.close {
      val st = conn.createStatement().open()
      try {
        st!!.executeUpdate(query)
      } catch (ex: Exception) {
        throw handleStatementError(ex, query)
      }
    }
  }

  override fun insert(queryTemplate: String, vararg paramValues: Any?): List<*> {
    val query = engine.buildQuery(queryTemplate, *paramValues)

    return Auto.close {
      val st = conn.createStatement().open()
      try {
        st!!.execute(query, Statement.RETURN_GENERATED_KEYS)
      } catch (ex: Exception) {
        throw handleStatementError(ex, query)
      }

      val result = arrayListOf<Any>()
      val rs = st.generatedKeys.open()
      while (rs!!.next()) {
        result.add(rs.getObject(1))
      }
      result
    }
  }

  override fun <T> select(mapper: (ResultSet) -> T, queryTemplate: String, vararg paramValues: Any?): List<T> {
    return doSelect({rs -> map(rs, mapper)}, queryTemplate, *paramValues)
  }

  override fun <T> sel(mapper: Mapper<ResultSet, T>, queryTemplate: String, vararg paramValues: Any?): List<T> {
    return select({rs -> mapper.map(rs)}, queryTemplate, *paramValues)
  }

  override fun <T> select(type: Class<T>, queryTemplate: String, vararg paramValues: Any?): List<T> {
    return doSelect({rs -> mapToClass(rs, type)}, queryTemplate, *paramValues)
  }

  override fun select(queryTemplate: String, vararg paramValues: Any?): List<List<Any?>> {
    return doSelect({rs -> mapToList(rs)}, queryTemplate, *paramValues)
  }

  private fun <T> doSelect(
    mapper: (ResultSet) -> List<T>,
    queryTemplate: String,
    vararg paramValues: Any?
  ): List<T> {
    val query = engine.buildQuery(queryTemplate, *paramValues)

    return Auto.close {
      val st = conn.createStatement().open()

      val rs = try {
        st!!.executeQuery(query).open()
      } catch (ex: Exception) {
        throw handleStatementError(ex, query)
      }

      mapper.invoke(rs!!)
    }
  }

  private fun <T> mapToClass(rs: ResultSet, type: Class<T>): List<T> {
    val result = arrayListOf<T>();

    val metaData = rs.metaData
    val columnClasses = arrayOfNulls<Class<*>>(metaData.columnCount)
    for (i in 1..metaData.columnCount) {
      columnClasses[i - 1] = Class.forName(metaData.getColumnClassName(i))
    }

    val columnValues = arrayOfNulls<Any?>(metaData.columnCount)
    while (rs.next()) {
      for (i in 1..metaData.columnCount) {
        columnValues[i - 1] = rs.getObject(i)
      }

      val ctr = type.getConstructor(*columnClasses)
      result.add(ctr.newInstance(*columnValues))
    }

    return result
  }

  private fun mapToList(rs: ResultSet): List<List<Any?>> {
    val result = arrayListOf<List<Any?>>()
    val metaData = rs.metaData
    while (rs.next()) {
      val values = arrayListOf<Any?>()
      for (i in 1..metaData.columnCount) {
        values.add(rs.getObject(i))
      }
      result.add(values)
    }
    return result
  }

  private fun <T> map(rs: ResultSet, mapper: (ResultSet) -> T): List<T> {
    val result = arrayListOf<T>()
    while (rs.next()) {
      result.add(mapper.invoke(rs))
    }
    return result
  }

  override fun updateMany(count: Int) {
    if (count <= 0) { throw IllegalStateException() }
  }

  override fun updateOne(count: Int) {
    if (count != 1) { throw IllegalStateException() }
  }

  override fun updateOneOrNone(count: Int) {
    if (count != 0 && count != 1) { throw IllegalStateException() }
  }

  override fun <T> takeMany(list: List<T>): List<T> {
    updateMany(list.size)
    return list
  }

  override fun <T> takeOne(list: List<T>): T {
    updateOne(list.size)
    return list.iterator().next()
  }

  override fun <T> takeOneOrNone(list: List<T>): T? {
    updateOneOrNone(list.size)
    return if (list.isEmpty()) null else list.iterator().next()
  }

  override fun <T> takeFirstColumn(list: List<List<*>>): List<T> = list.map { e -> e.iterator().next() as T }

  private fun <E> handleStatementError(ex: E, query: String): E {
    LOG.error("Failed query: $query", ex)
    return ex
  }
}
