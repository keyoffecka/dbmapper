package net.ofk.dbmapper.defaults.api;

import java.sql.Connection;

/**
 * Defines and maintains connection life cycle:
 * every connection should be acquired, used and released or invalidated within its session.
 * If during the connection usage an error happens with the connection,
 * this connection should be invalidated rather than released.
 * Several connections may exist within the same session.
 *
 * Sessions are introduced and are only used by the default transaction implementation.
 * Other transaction implementations may use their own approaches to maintain
 * connection lifecycles.
 */
public interface Session {
  /**
   * Returns a connection.
   * Every acquired connection must be released or marked as invalid.
   */
  Connection acquire() throws Exception;

  /**
   * Optionally releases resources associated with the connection.
   * It's up to an implementation to decide
   * whether to re-use the connection or to create a new connection
   * when new ones are acquired.
   */
  void release(Connection conn) throws Exception;

  /**
   * Marks a particular connection as invalid and re-throws an exception.
   * Resources of the invalid connections may be kept unreleased.
   * Invalid connections will not be re-used.
   * It's up to an implementation to decide
   * whether the session may continue returning new connections
   * if one of its connections is invalid.
   */
  void invalidate(Connection conn, Exception prev) throws Exception;
}