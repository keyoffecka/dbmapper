package net.ofk.dbmapper.api;

public interface Mapper<R, T> {
  T map(R r) throws Throwable;
}
