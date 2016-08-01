package net.ofk.dbmapper.api;

/**
 * A helper interface to be used within a Java code.
 * The #execute() method will be called inside a transaction,
 * and the executing code will receive a storage instance
 * to run queries and an accessor object to get query templates.
 */
public interface TransactionExecutableWithQueries<Q, R> {
  /**
   * Executes queries on the provided storage
   * using query templates accessible via
   * query accessor object.
   */
  void execute(Q queries, Storage<R> storage) throws Throwable;
}
