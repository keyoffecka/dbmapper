package net.ofk.dbmapper.api;

/**
 * Transaction is a set of queries executed on one connection.
 * If all the queries succeed till the end of a transaction all of them are applied.
 * If one of the queries fails all previous queries will be rolled back and the transaction ends immediately.
 *
 * @param R - type of the raw results as returned by the underlying data storage.
 */
public interface Transaction<R> {
  /**
   * Executes a code inside the current transaction.
   * If no transaction has yet started in the current thread
   * a new one is created.
   * The code which runs queries will receive a storage object
   * and a query accessor object of the specified @param type.
   */
  <Q, T> T call(Class<Q> type, Transaction.TransactionCallableWithQueries<Q, R, T> executable);

  <Q> void exec(Class<Q> type, Transaction.TransactionExecutableWithQueries<Q, R> executable);

  /**
   * Executes a code inside the current transaction.
   * If no transaction has yet started in the current thread
   * a new one is created.
   * The code which runs queries will receive only a storage object.
   */
  <T> T call(Transaction.TransactionCallable<R, T> executable);

  void exec(Transaction.TransactionExecutable<R> executable);

  /**
   * A helper interface to be used within a Java code.
   * The #execute() method will be called inside a transaction,
   * and the executing code will receive a storage instance
   * to run queries.
   */
  interface TransactionCallable<R, T> {
    /**
     * Executes queries on the provided storage.
     */
    T execute(Storage<R> storage) throws Throwable;
  }

  /**
   * The #execute() method will be called inside a transaction,
   * and the executing code will receive a storage instance
   * to run queries and an accessor object to get query templates.
   */
  interface TransactionCallableWithQueries<Q, R, T> {
    /**
     * Executes queries on the provided storage
     * using query templates accessible via
     * query accessor object.
     */
    T execute(Q queries, Storage<R> storage) throws Throwable;
  }

  /**
   * The #execute() method will be called inside a transaction,
   * and the executing code will receive a storage instance
   * to run queries.
   */
  interface TransactionExecutable<R> {
    /**
     * Executes queries on the provided storage.
     */
    void execute(Storage<R> storage) throws Throwable;
  }

  /**
   * The #execute() method will be called inside a transaction,
   * and the executing code will receive a storage instance
   * to run queries and an accessor object to get query templates.
   */
  interface TransactionExecutableWithQueries<Q, R> {
    /**
     * Executes queries on the provided storage
     * using query templates accessible via
     * query accessor object.
     */
    void execute(Q queries, Storage<R> storage) throws Throwable;
  }
}

