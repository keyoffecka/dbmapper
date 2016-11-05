package net.ofk.dbmapper.defaults.api;

import java.sql.Connection;

/**
 * The main responsibility of engines is to prepare queries
 * valid for execution on a specific DB management system.
 *
 * Engines are introduced and are only used by the default transaction implementation.
 * Other transaction implementations may use their own approaches to access or modify data.
 */
public interface Engine {
  /**
   * Engines to produce queries valid for a particular DB management system
   * may need to prepare connections on which the queries are going to be executed.
   * This method should be called after acquiring a connection but before building
   * the first query that will be executed on the connection.
   */
  void prepareConnection(Connection conn);

  /**
   * Sets query parameters using syntax specific to a DB management system.
   */
  String buildQuery(String pattern, Object... paramValues);

  /**
   * Alters a value so it can be used as a string value
   * in queries without breaking them, causing syntax errors or SQL-injections.
   */
  String escape(String value);

  /**
   * Query resources specific for the engine implementation
   * should have postfix as defined by this property.
   * One engine implementation should always return the same value
   * and it should be documented.
   */
  String variant();
}