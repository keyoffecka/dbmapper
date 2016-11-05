package net.ofk.dbmapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ofk.dbmapper.api.Transaction;
import net.ofk.dbmapper.defaults.impl.DefaultEngine;
import net.ofk.dbmapper.defaults.impl.DefaultFactory;
import net.ofk.dbmapper.defaults.impl.DefaultSession;
import net.ofk.dbmapper.defaults.impl.DefaultTransaction;
import org.junit.Assert;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class FunctionalTest {
  private final DefaultTransaction tx = new DefaultTransaction(new DefaultSession(new DefaultFactory("jdbc:h2:mem:test;DATABASE_TO_UPPER=FALSE", null, null)), new DefaultEngine());

  @Test
  public void testSelectObject() throws Exception {
    Q[] qq = {null, null};

    this.tx.exec(Q.class, (q, s) -> {
      qq[0] = q;
      s.update(q.create());
      s.insert("insert into A(name) values('a1'),('a2'),('a3');");

      this.tx.exec(ss -> {
        Assert.assertEquals(
          Lists.newArrayList(
            new FunctionalTest.A(3, "a3"),
            new FunctionalTest.A(1, "a1")
          ), ss.select(FunctionalTest.A.class, "select * from A where id!=:id and id!=:id order by id desc;", "id", 2)
        );
      });
    });

    this.tx.exec(Q.class, (q, s) -> {
      qq[1] = q;
    });

    Assert.assertSame(qq[0], qq[1]);
  }

  @Test
  public void testSelectList() throws Exception {
    this.tx.exec(Q.class, (q, s) -> {
      s.update(q.create());
      s.insert("insert into A(name) values('a1'),('a2'),('a3');");

      this.tx.exec(ss -> {
        Assert.assertEquals(
          Lists.newArrayList(
            Lists.newArrayList(3, "a3"),
            Lists.newArrayList(1, "a1")
          ), ss.select("select * from A where id!=:id and id!=:id order by id desc;", "id", 2)
        );
      });
    });
  }

  @Test
  public void testSelectMap() throws Exception {
    this.tx.exec(Q.class, (q, s) -> {
      s.update(q.create());
      s.insert("insert into A(name) values('a1'),('a2'),('a3');");

      this.tx.exec(ss ->
        Assert.assertEquals(
          Lists.newArrayList(
            new FunctionalTest.A(4, "a3a"),
            new FunctionalTest.A(2, "a1a")
          ), ss.select(rs -> {
            return new FunctionalTest.A(rs.getInt("id") + 1, rs.getString("name") + "a");
          }, "select * from A where id!=:id and id!=:id order by id desc;", "id", 2)
        )
      );
    });
  }

  @Test
  public void testBadSelect() {
    try {
      this.tx.exec(Q.class, (q, s) -> {
        s.select("select A");
      });
    } catch(final Exception ex) {
      Assert.assertTrue(ex instanceof SQLException);
    }
  }

  @Test
  public void testUpdate() throws Exception {
    this.tx.exec(Q.class, (q, storage) -> {
      storage.update(q.create());
      storage.insert("insert into A(name) values('a1'),('a2'),('a3');");

      this.tx.exec(s -> {
        Assert.assertEquals(
          2,
          s.update("update A set name=:name where name in (:names)", "name", "a", "names", ImmutableList.of("a1", "a3"))
        );

        Assert.assertEquals(
          Lists.newArrayList(
            new FunctionalTest.A(1, "a"),
            new FunctionalTest.A(2, "a2"),
            new FunctionalTest.A(3, "a")
          ),
          s.select(FunctionalTest.A.class, "select * from A order by id")
        );
      });
    });
  }

  @Test
  public void testBadUpdate() throws Exception {
    Transaction<ResultSet> tx = new DefaultTransaction(new DefaultSession(new DefaultFactory("jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE", null, null)), new DefaultEngine());

    tx.exec(Q.class, (q, s) -> {
      s.update(q.create());
    });

    try {
      tx.exec(s -> {
        s.insert("insert into A(name) values('a1'),('a2'),('a3');");

        Assert.assertFalse(s.insert("select * from A").isEmpty());

        tx.exec(ss -> {
          ss.update("update A");
        });
      });
    } catch (final Exception ex) {
      Assert.assertTrue(ex instanceof SQLException);
    }

    tx.exec(s -> {
      Assert.assertTrue(s.insert("select * from A").isEmpty());
    });
  }

  @Test
  public void testBadInsert() throws Exception {
    DefaultTransaction tx = new DefaultTransaction(new DefaultSession(new DefaultFactory("jdbc:h2:mem:test3;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE", null, null)), new DefaultEngine());

    tx.exec(Q.class, (q, s) -> {
      s.update(q.create());
    });

    try {
      tx.exec(s -> {
        s.insert("insert into A(name) values('a1'),('a2'),('a3');");

        Assert.assertFalse(s.insert("select * from A").isEmpty());

        tx.exec(ss -> {
          ss.insert("insert into A");
        });
      });
    } catch (final Exception ex) {
      Assert.assertTrue(ex instanceof SQLException);
    }

    tx.exec(s -> {
      Assert.assertTrue(s.insert("select * from A").isEmpty());
    });
  }

  @Test
  public void testInsert() throws Exception {
    this.tx.exec(Q.class, (q, storage) -> {
      storage.update(q.create());

      this.tx.exec(s -> {
        Assert.assertEquals(
          ImmutableList.of(1),
          s.insert("insert into A(name) values(:p1)", "p1", "a1")
        );
        Assert.assertEquals(
          ImmutableList.of(2),
          s.insert("insert into A(name) values(:p2)", "p2", "a2")
        );
      });
    });
  }

  public static class A {
    public final int id;
    public final String name;

    public A(final Integer id, final String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public boolean equals(final Object object) {
      boolean result = this == object;

      if (!result) {
        FunctionalTest.A instance = (FunctionalTest.A) object;
        if (object != null && this.getClass().equals(object.getClass())) {
          result = Objects.equals(this.id, instance.id)
                && Objects.equals(this.name, instance.name);
        }
      }

      return result;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(this.id)
           ^ Objects.hashCode(this.name);
    }
  }
}
