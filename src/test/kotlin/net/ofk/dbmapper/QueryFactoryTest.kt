package net.ofk.dbmapper

import org.junit.Assert
import org.junit.Test
import java.lang.reflect.UndeclaredThrowableException

class QueryFactoryTest {
  @Test
  fun testCreate() {
    Assert.assertFalse(
      QueryFactory().create(Queries::class.java) == QueryFactory().create(Queries::class.java)
    )

    var q = QueryFactory().create(Queries::class.java)
    Assert.assertEquals("select", q.query())
    Assert.assertEquals("qry", q.qry())

    q = QueryFactory().create(Queries::class.java, "")
    Assert.assertEquals("select", q.query())
    Assert.assertEquals("qry", q.qry())

    q = QueryFactory().create(Queries::class.java, "mysql")
    Assert.assertEquals("select", q.query())
    Assert.assertEquals("qry", q.qry())

    var tq = QueryFactory().create(TestQueries::class.java)
    Assert.assertEquals("query", tq.query0())
    Assert.assertEquals("01.10 text", tq.query1(1.1))
    Assert.assertEquals("missing", tq.missing(1))

    tq = QueryFactory().create(TestQueries::class.java, "")
    Assert.assertEquals("query", tq.query0())
    Assert.assertEquals("01.10 text", tq.query1(1.1))
    Assert.assertEquals("missing", tq.missing(1))

    tq = QueryFactory().create(TestQueries::class.java, "mysql")
    Assert.assertEquals("query", tq.query0())
    Assert.assertEquals("1.1 data", tq.query1(1.1))
    Assert.assertEquals("missing1", tq.missing(1))
    Assert.assertEquals("_missing", tq._missing(1))

    var btq = QueryFactory().create(BadTestQueries::class.java)
    try {
      btq.q()
      Assert.fail()
    } catch(e: UndeclaredThrowableException) {
      Assert.assertEquals("No default resource found for net.ofk.dbmapper.BadTestQueries", e.undeclaredThrowable.message)
    }

    try {
      btq = QueryFactory().create(BadTestQueries::class.java, "mysql")
      btq.q()
    } catch(e: UndeclaredThrowableException) {
      Assert.assertEquals("No default resource found for net.ofk.dbmapper.BadTestQueries", e.undeclaredThrowable.message)
    }
  }
}

interface Queries {
  fun query(): String
  fun qry(): String
}

interface TestQueries {
  fun query0(): String
  fun query1(value: Double): String
  fun missing(p: Int): String
  fun _missing(p: Int): String
}

interface BadTestQueries {
  fun q(): String
}

