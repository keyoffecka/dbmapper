package net.ofk.dbmapper.api;

import java.util.List;

/**
 * Provides access to a data storage,
 * allows to store, modify data or fetch them.
 * Basically, this is intended to be used with RDBM systems,
 * but in theory can be used with another types of data storages.
 *
 * R - type of the raw results as returned by the underlying data storage.
 */
public interface Storage<R> {
  /**
   * Executes a query which stores new data.
   * The result should contain ids of the inserted entities.
   */
  List<?> insert(String queryTemplate, Object... paramValues) throws Throwable;

  /**
   * Executes a query which updates existing entities.
   * The result is the amount of the affected entities.
   */
  int update(String queryTemplate, Object... paramValues) throws Throwable;

  /**
   * Returns a list of records.
   * Mapper will be responsible for creating elements of the list from the provided raw results.
   */
  <T> List<T> select(Mapper<R, T> mapper, String queryTemplate, Object... paramValues) throws Throwable;

  /**
   * Returns a list of records.
   * The default mapper will create an object of the given @param type.
   */
  <T> List<T> select(Class<T> type, String queryTemplate, Object... paramValues) throws Throwable;

  /**
   * Returns a list of records.
   * Every record will contain a list of field values.
   */
  List<List<?>> select(String queryTemplate, Object... paramValues) throws Throwable;

  /**
   * An utility method which checks if the amount of updated records is not null.
   */
  void updateMany(int count);

  /**
   * An utility method which checks if exactly one record has been updated.
   */
  void updateOne(int count);

  /**
   * An utility method which checks if not more than one record has been updated.
   */
  void updateOneOrNone(int count);

  /**
   * Returns the provided list or throws an IllegalStateException
   * if the list has no values.
   */
  <T> List<T> takeMany(List<T> list);

  /**
   * Returns the first element of the provided list
   * or throws an IllegalStateException
   * if the list has no values or has more than one value.
   */
  <T> T takeOne(List<T> list);

  /**
   * Returns the first element of the provided list
   * or throws an IllegalStateException if the list has more than one value.
   * Returns null if the list is empty.
   */
  <T> T takeOneOrNone(List<T> list);

  /**
   * Returns a list of elements which are the first elements of the inner lists.
   */
  <T> List<T> takeFirstColumn(List<List<?>> list);

  /**
   * Alters a value so it can be used as a string value
   * in queries without breaking them, causing syntax errors or SQL-injections.
   */
  String escape(String value);

  interface Mapper<R, T> {
    T map(R r) throws Throwable;
  }
}

