package net.ofk.dbmapper.defaults.impl;

import net.ofk.dbmapper.defaults.api.Factory;
import net.ofk.dbmapper.defaults.api.Session;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This session doesn't close connections on #release,
 * it will continue returning new connections
 * if one of its connections marked as invalid.
 * The same connection may be acquired from different threads,
 * but it's guaranteed that the connection is not acquired
 * by two threads at the same time.
 * If one thread acquires a connection,
 * the connection will be re-used only by the that thread
 * until the thread release the connection;
 * other threads will use their own connections at the same time.
 * After the connection is released it may be acquired by another thread.
 */
public class CachingSession implements Session {
  private final Factory factory;

  public CachingSession(final Factory factory) {
    this.factory = factory;
  }

  final ThreadLocal<Connection> connections = new ThreadLocal<>();
  Connection freeConnection = null;

  @Override
  public Connection acquire() {
    Connection conn = null;
    synchronized(this.connections) {
      conn = this.connections.get();
      if (conn == null) {
        if (this.freeConnection == null) {
          conn = this.factory.create();
        } else {
          conn = this.freeConnection;
          this.freeConnection = null;
        }
        this.connections.set(conn);
      }
    }
    return conn;
  }

  @Override
  public void release(final Connection conn) throws SQLException {
    synchronized(this.connections) {
      this.validationConnection(conn);

      this.doRelease(conn);
    }
  }

  @Override
  public void invalidate(final Connection conn, final Exception prev) throws Exception {
    synchronized(this.connections) {
      this.validationConnection(conn);

      try {
        this.doRelease(conn);
      } catch (final Exception ex) {
        ex.addSuppressed(prev);
        throw ex;
      } finally {
        this.cleanFreeConnection(conn);
      }

      throw prev;
    }
  }

  void doRelease(final Connection conn) throws SQLException {
    this.connections.set(null);

    if (this.freeConnection == null) {
      this.freeConnection = conn;
    } else {
      if (conn == this.freeConnection) {
        throw new IllegalStateException("This shouldn't have happened, but...");
      }
      conn.close();
    }
  }

  void cleanFreeConnection(final Connection conn) {
    if (this.freeConnection != null && conn == this.freeConnection) {
      this.freeConnection = null;
    }
  }

  void validationConnection(final Connection conn) {
    if (this.connections.get() == null || this.connections.get() != conn) {
      throw new IllegalStateException("Unknown connection");
    }
  }
}
