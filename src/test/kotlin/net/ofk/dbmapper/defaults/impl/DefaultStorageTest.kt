package net.ofk.dbmapper.defaults.impl

import net.ofk.dbmapper.defaults.api.Engine
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import java.sql.Connection

class DefaultStorageTest {
  private val conn = Mockito.mock(Connection::class.java)
  private val engine = Mockito.mock(Engine::class.java)
  private val s = DefaultStorage(conn, engine)

  @Test
  fun testTakeFirstColumn() {
    Assert.assertEquals(listOf("a", 2), s.takeFirstColumn<Any>(listOf(listOf("a", 1), listOf(2, "b"))))
  }

  @Test
  fun testTakeMany() {
    val l1 = listOf(1, 2)
    Assert.assertSame(l1, s.takeMany(l1))

    val l2 = listOf(1)
    Assert.assertSame(l2, s.takeMany(l2))

    try {
      s.takeMany(listOf<Any>())
      Assert.fail();
    } catch(ex: IllegalStateException) {
    }
  }

  @Test
  fun testTakeOne() {
    Assert.assertEquals(11, s.takeOne(listOf(11)))

    try {
      s.takeOne(listOf(1, 2))
      Assert.fail();
    } catch(ex: IllegalStateException) {
    }

    try {
      s.takeOne(listOf<Any>())
      Assert.fail();
    } catch(ex: IllegalStateException) {
    }
  }

  @Test
  fun testTakeOneOrNone() {
    Assert.assertNull(s.takeOneOrNone(listOf<Any>()))

    Assert.assertEquals(11, s.takeOneOrNone(listOf(11)))

    try {
      s.takeOneOrNone(listOf(1, 2))
      Assert.fail();
    } catch(ex: IllegalStateException) {
    }
  }

  @Test
  fun escape() {
    s.escape("abc")

    Mockito.verify(engine).escape("abc")
  }
}