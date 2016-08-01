package net.ofk.dbmapper.api

/**
 * Transaction is a set of queries executed on one connection.
 * If all the queries succeed till the end of a transaction all of them are applied.
 * If one of the queries fails all previous queries will be rolled back and the transaction ends immediately.
 *
 * @param R - type of the raw results as returned by the underlying data storage.
 */
interface Transaction<R> {
  /**
   * Executes a code inside the current transaction.
   * If no transaction has yet started in the current thread
   * a new one is created.
   * The code which runs queries will receive a storage object
   * and a query accessor object of the specified @param type.
   *
   * This is a helper method to be called from within Java code.
   */
  fun <Q> exec(type: Class<Q>, executable: TransactionExecutableWithQueries<Q, R>)

  /**
   * Executes a code inside the current transaction.
   * If no transaction has yet started in the current thread
   * a new one is created.
   * The code which runs queries will receive a storage object
   * and a query accessor object of the specified @param type.
   */
  fun <T,Q> execute(type: Class<Q>, executable: (queries: Q, storage: Storage<R>) -> T): T

  /**
   * Executes a code inside the current transaction.
   * If no transaction has yet started in the current thread
   * a new one is created.
   * The code which runs queries will receive only a storage object.
   *
   * This is a helper method to be called from within Java code.
   */
  fun exec(executable: TransactionExecutable<R>)

  /**
   * Executes a code inside the current transaction.
   * If no transaction has yet started in the current thread
   * a new one is created.
   * The code which runs queries will receive only a storage object.
   */
  fun <T> execute(executable: (storage: Storage<R>) -> T): T

}

