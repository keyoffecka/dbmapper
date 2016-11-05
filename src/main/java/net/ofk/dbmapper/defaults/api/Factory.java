package net.ofk.dbmapper.defaults.api;

import java.sql.Connection;

/**
 * The only responsibility of the factory is to produce connections.
 *
 * Factories are introduced and are only used by the default session implementation.
 * Other session implementations may use their own approaches to produce connections.
 */
public interface Factory {
  /**
   * Every time the method is called a new connection should be created
   * and returned.
   */
  Connection create();
}