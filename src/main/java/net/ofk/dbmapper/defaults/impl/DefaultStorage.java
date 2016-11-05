package net.ofk.dbmapper.defaults.impl;

import net.ofk.dbmapper.defaults.api.Engine;
import net.ofk.dbmapper.api.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation of the storage which is needed only and created by the default transaction implementation.
 */
class DefaultStorage implements Storage<ResultSet> {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultStorage.class);

  private final Connection conn;
  private final Engine engine;

  DefaultStorage(final Connection conn, final Engine engine) {
    this.conn = conn;
    this.engine = engine;
  }

  @Override
  public int update(final String queryTemplate, final Object... paramValues) throws Exception {
    String query = this.engine.buildQuery(queryTemplate, paramValues);

    Integer result = null;
    try (
      Statement st = this.conn.createStatement();
    ) {
      try {
        result = st.executeUpdate(query);
      } catch (final Exception ex) {
        throw this.handleStatementError(ex, query);
      }
    }

    return result;
  }

  @Override
  public List<?> insert(final String queryTemplate, final Object... paramValues) throws Exception {
    List<Object> result = new ArrayList<>();

    String query = this.engine.buildQuery(queryTemplate, paramValues);

    try (
      Statement st = this.conn.createStatement();
    ) {
      try {
        st.execute(query, Statement.RETURN_GENERATED_KEYS);
      } catch (final Exception ex) {
        throw this.handleStatementError(ex, query);
      }

      try (
        ResultSet rs = st.getGeneratedKeys();
      ) {
        while (rs.next()) {
          result.add(rs.getObject(1));
        }
      }
    }

    return result;
  }

  @Override
  public <T> List<T> select(final Mapper<ResultSet, T> mapper, final String queryTemplate, final Object... paramValues) throws Throwable {
    return this.doSelect(rs -> this.map(rs, mapper), queryTemplate, paramValues);
  }

  @Override
  public <T> List<T> select(final Class<T> type, final String queryTemplate, final Object... paramValues) throws Throwable {
    return this.doSelect(rs -> this.mapToClass(rs, type), queryTemplate, paramValues);
  }

  @Override
  public List<List<?>> select(final String queryTemplate, final Object... paramValues) throws Throwable {
    return this.doSelect(this::mapToList, queryTemplate, paramValues);
  }

  private <T> List<T> doSelect(
    final Mapper<ResultSet, List<T>> mapper,
    final String queryTemplate,
    final Object... paramValues
  ) throws Throwable {
    List<T> result = null;

    String query = this.engine.buildQuery(queryTemplate, paramValues);

    try (
      Statement st = this.conn.createStatement();
    ) {
      ResultSet rs = null;
      try {
        try {
          rs = st.executeQuery(query);
        } catch (final Throwable ex) {
          throw this.handleStatementError(ex, query);
        }

        result = mapper.map(rs);
      } finally {
        if (rs != null) {
          rs.close();
        }
      }
    }

    return result;
  }

  private <T> List<T> mapToClass(final ResultSet rs, final Class<T> type) throws Exception {
    List<T> result = new ArrayList<>();

    ResultSetMetaData metaData = rs.getMetaData();
    Class<?>[] columnClasses = new Class[metaData.getColumnCount()];
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      columnClasses[i - 1] = Class.forName(metaData.getColumnClassName(i));
    }

    Object[] columnValues = new Object[metaData.getColumnCount()];
    while (rs.next()) {
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        columnValues[i - 1] = rs.getObject(i);
      }

      Constructor<T> ctr = type.getConstructor(columnClasses);
      result.add(ctr.newInstance(columnValues));
    }

    return result;
  }

  private List<List<?>> mapToList(final ResultSet rs) throws Exception {
    List<List<?>> result = new ArrayList<>();
    ResultSetMetaData metaData = rs.getMetaData();
    while (rs.next()) {
      List<Object> values = new ArrayList<>();
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        values.add(rs.getObject(i));
      }
      result.add(values);
    }
    return result;
  }

  private <T> List<T> map(final ResultSet rs, final Mapper<ResultSet, T> mapper) throws Throwable {
    List<T> result = new ArrayList<>();
    while (rs.next()) {
      result.add(mapper.map(rs));
    }
    return result;
  }

  @Override
  public void updateMany(final int count) {
    if (count <= 0) { throw new IllegalStateException(); }
  }

  @Override
  public void updateOne(final int count) {
    if (count != 1) { throw new IllegalStateException(); }
  }

  @Override
  public void updateOneOrNone(final int count) {
    if (count != 0 && count != 1) { throw new IllegalStateException(); }
  }

  @Override
  public <T> List<T> takeMany(final List<T> list) {
    this.updateMany(list.size());
    return list;
  }

  @Override
  public <T> T takeOne(final List<T> list) {
    this.updateOne(list.size());
    return list.iterator().next();
  }

  @Override
  public <T> T takeOneOrNone(final List<T> list) {
    this.updateOneOrNone(list.size());
    return list.isEmpty() ? null : list.iterator().next();
  }

  @Override
  public <T> List<T> takeFirstColumn(final List<List<?>> list) {
    return list.stream().map(e -> (T) e.iterator().next()).collect(Collectors.toList());
  }

  private <E> E handleStatementError(final E ex, final String query) {
    DefaultStorage.LOG.error("Failed query: " + query, ex);
    return ex;
  }

  @Override
  public String escape(final String value) {
    return this.engine.escape(value);
  }
}
