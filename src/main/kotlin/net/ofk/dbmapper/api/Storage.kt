package net.ofk.dbmapper.api

/**
 * Provides access to a data storage,
 * allows to store, modify data or fetch them.
 * Basically, this is intended to be used with RDBM systems,
 * but in theory can be used with another types of data storages.
 *
 * @param R - type of the raw results as returned by the underlying data storage.
 */
interface Storage<R> {
  /**
   * Executes a query which stores new data.
   * The result should contain ids of the inserted entities.
   */
  fun insert(queryTemplate: String, vararg paramValues: Any?): List<*>

  /**
   * Executes a query which updates existing entities.
   * The result is the amount of the affected entities.
   */
  fun update(queryTemplate: String, vararg paramValues: Any?): Int

  /**
   * Returns a list of records.
   * Mapper will be responsible for creating elements of the list from the provided raw results.
   *
   * A helper method, to be called from java code.
   */
  fun <T> sel(mapper: Mapper<R, T>, queryTemplate: String, vararg paramValues: Any?): List<T>

  /**
   * Returns a list of records.
   * Mapper will be responsible for creating elements of the list from the provided raw results.
   */
  fun <T> select(mapper: (R) -> T, queryTemplate: String, vararg paramValues: Any?): List<T>

  /**
   * Returns a list of records.
   * The default mapper will create an object of the given @param type.
   */
  fun <T> select(type: Class<T>, queryTemplate: String, vararg paramValues: Any?): List<T>

  /**
   * Returns a list of records.
   * Every record will contain a list of field values.
   */
  fun select(queryTemplate: String, vararg paramValues: Any?): List<List<Any?>>

  /**
   * An utility method which checks if the amount of updated records is not null.
   */
  fun updateMany(count: Int)

  /**
   * An utility method which checks if exactly one record has been updated.
   */
  fun updateOne(count: Int)

  /**
   * An utility method which checks if not more than one record has been updated.
   */
  fun updateOneOrNone(count: Int)

  /**
   * Returns the provided list or throws an IllegalStateException
   * if the list has no values.
   */
  fun <T> takeMany(list: List<T>): List<T>

  /**
   * Returns the first element of the provided list
   * or throws an IllegalStateException
   * if the list has no values or has more than one value.
   */
  fun <T> takeOne(list: List<T>): T

  /**
   * Returns the first element of the provided list
   * or throws an IllegalStateException if the list has more than one value.
   * Returns null if the list is empty.
   */
  fun <T> takeOneOrNone(list: List<T>): T?

  /**
   * Returns a list of elements which are the first elements of the inner lists.
   */
  fun <T> takeFirstColumn(list: List<List<*>>): List<T>
}

