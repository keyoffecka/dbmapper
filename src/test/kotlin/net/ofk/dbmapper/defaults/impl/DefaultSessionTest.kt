package net.ofk.dbmapper.defaults.impl

import net.ofk.dbmapper.defaults.api.Factory
import net.ofk.kutils.JRE8Utils
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import java.sql.Connection

class DefaultSessionTest {
  private val f = Mockito.mock(Factory::class.java)
  private val s = DefaultSession(f)

  @Test
  fun testReleaseFailure() {
    val conn = Mockito.mock(Connection::class.java)

    try {
      s.release(conn)
      Assert.fail()
    } catch(ex: IllegalStateException) {
    }
    Mockito.verify(conn, Mockito.never()).close()
  }

  @Test
  fun testInvalidateFailure() {
    val conn = Mockito.mock(Connection::class.java)

    val e = Exception()
    try {
      s.invalidate(conn, e)
      Assert.fail()
    } catch(ex: IllegalStateException) {
      Assert.assertArrayEquals(arrayOf<Throwable>(e), JRE8Utils.INSTANCE.getSuppressed(ex))
    }

    Mockito.doReturn(conn).`when`(f).create()

    Assert.assertSame(conn, s.acquire())

    try {
      s.invalidate(conn, e)
      Assert.fail()
    } catch(ex: Exception) {
      Assert.assertSame(e, ex)
    }

    Mockito.verify(conn).close()
  }

  @Test
  fun testRelease() {
    val conn = Mockito.mock(Connection::class.java)

    Mockito.doReturn(conn).`when`(f).create()

    Assert.assertSame(conn, s.acquire())

    s.release(conn)

    Mockito.verify(conn).close()
  }
}