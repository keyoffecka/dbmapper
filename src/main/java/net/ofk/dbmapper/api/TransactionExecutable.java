package net.ofk.dbmapper.api;

/**
 * A helper interface to be used within a Java code.
 * The #execute() method will be called inside a transaction,
 * and the executing code will receive a storage instance
 * to run queries.
 */
public interface TransactionExecutable<R> {
  /**
   * Executes queries on the provided storage.
   */
  void execute(Storage<R> storage) throws Throwable;
}
