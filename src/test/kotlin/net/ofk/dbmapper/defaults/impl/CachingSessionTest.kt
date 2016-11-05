package net.ofk.dbmapper.defaults.impl

import net.ofk.dbmapper.defaults.api.Factory
import net.ofk.kutils.JRE8Utils
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import java.sql.Connection

class CachingSessionTest {
  @Test
  fun testAcquire() {
    val f = Mockito.mock(Factory::class.java)
    val c1 = Mockito.mock(Connection::class.java)
    val c2 = Mockito.mock(Connection::class.java)

    Mockito.doReturn(c1).doReturn(c2).`when`(f).create()

    val s = CachingSession(f)
    Assert.assertSame(c1, s.acquire())
    Assert.assertNull(s.freeConnection)
    Assert.assertSame(c1, s.acquire())
    Assert.assertNull(s.freeConnection)

    Mockito.verify(f, Mockito.times(1)).create()

    var ex: Throwable? = null
    val that: Object = this as Object
    synchronized (that) {
      Thread({
        try {
          Assert.assertSame(c2, s.acquire())
          Assert.assertNull(s.freeConnection)
        } catch (th: Throwable) {
          ex = th
        } finally {
          synchronized(that) {
            that.notify()
          }
        }
      }).start()

      that.wait()
    }

    if (ex != null) {
      throw ex!!
    }

    Assert.assertNull(s.freeConnection)

    Mockito.verify(f, Mockito.times(2)).create()
  }

  @Test
  fun testAcquireFreeConnection() {
    val f = Mockito.mock(Factory::class.java)
    val c1 = Mockito.mock(Connection::class.java)
    val c2 = Mockito.mock(Connection::class.java)

    val s = CachingSession(f)
    s.freeConnection = c1
    Assert.assertSame(c1, s.acquire())
    Assert.assertNull(s.freeConnection)
    Assert.assertSame(c1, s.acquire())
    Assert.assertNull(s.freeConnection)

    Mockito.verify(f, Mockito.never()).create()
    s.freeConnection = c2

    var ex: Throwable? = null
    val that: Object = this as Object
    synchronized (that) {
      Thread({
        try {
          Assert.assertSame(c2, s.acquire())
          Assert.assertNull(s.freeConnection)
        } catch (th: Throwable) {
          ex = th
        } finally {
          synchronized(that) {
            that.notify()
          }
        }
      }).start()

      that.wait()
    }

    if (ex != null) {
      throw ex!!
    }

    Assert.assertNull(s.freeConnection)

    Mockito.verify(f, Mockito.never()).create()
  }

  @Test
  fun testValidateConnection() {
    val f = Mockito.mock(Factory::class.java)
    val s = CachingSession(f)

    val c1 = Mockito.mock(Connection::class.java)
    val c2 = Mockito.mock(Connection::class.java)

    try {
      s.validationConnection(null)
      Assert.fail()
    } catch (ex: IllegalStateException) {
      Assert.assertEquals("Unknown connection", ex.message)
    }

    try {
      s.validationConnection(c1)
      Assert.fail()
    } catch (ex: IllegalStateException) {
      Assert.assertEquals("Unknown connection", ex.message)
    }

    s.connections.set(c1)

    try {
      s.validationConnection(c2)
      Assert.fail()
    } catch (ex: IllegalStateException) {
      Assert.assertEquals("Unknown connection", ex.message)
    }
  }

  @Test
  fun testReleaseForeignConnection() {
    val f = Mockito.mock(Factory::class.java)
    val c1 = Mockito.mock(Connection::class.java)

    val s = Mockito.spy(CachingSession(f))
    val e = IllegalStateException()

    Mockito.doThrow(e).`when`(s).validationConnection(c1)

    try {
      s.release(c1)
      Assert.fail()
    } catch (ex: IllegalStateException) {
      Assert.assertSame(e, ex)
    }
  }

  @Test
  fun testInvalidateForeignConnection() {
    val f = Mockito.mock(Factory::class.java)
    val c1 = Mockito.mock(Connection::class.java)

    val s = Mockito.spy(CachingSession(f))
    val e = IllegalStateException()
    val ee = Exception()

    Mockito.doThrow(e).`when`(s).validationConnection(c1)

    try {
      s.invalidate(c1, ee)
      Assert.fail()
    } catch (ex: IllegalStateException) {
      Assert.assertSame(e, ex)
      Assert.assertTrue(JRE8Utils.INSTANCE.getSuppressed(ex).isEmpty())
    }
  }

  @Test
  fun testInvalidate() {
    val f = Mockito.mock(Factory::class.java)
    val c1 = Mockito.mock(Connection::class.java)
    val e = Exception()

    Mockito.doReturn(c1).`when`(f).create()

    val s = Mockito.spy(CachingSession(f))
    Mockito.doNothing().`when`(s).doRelease(c1)
    Mockito.doNothing().`when`(s).cleanFreeConnection(c1)

    s.acquire()

    try {
      s.invalidate(c1, e)
      Assert.fail()
    } catch (ex: Exception) {
      Assert.assertSame(e, ex)
    }

    Mockito.verify(s).doRelease(c1)
    Mockito.verify(s).cleanFreeConnection(c1)
  }

  @Test
  fun testInvalidateWithSuppression() {
    val f = Mockito.mock(Factory::class.java)
    val c1 = Mockito.mock(Connection::class.java)
    val e = Exception()
    val ee = RuntimeException()

    Mockito.doReturn(c1).`when`(f).create()

    val s = Mockito.spy(CachingSession(f))
    Mockito.doThrow(ee).`when`(s).doRelease(c1)
    Mockito.doNothing().`when`(s).cleanFreeConnection(c1)

    s.acquire()

    try {
      s.invalidate(c1, e)
      Assert.fail()
    } catch (ex: Exception) {
      Assert.assertSame(ee, ex)
      Assert.assertArrayEquals(arrayOf(e), JRE8Utils.INSTANCE.getSuppressed(ex))
    }

    Mockito.verify(s).doRelease(c1)
    Mockito.verify(s).cleanFreeConnection(c1)
  }

  @Test
  fun testRelease() {
    val f = Mockito.mock(Factory::class.java)
    val c1 = Mockito.mock(Connection::class.java)
    val c2 = Mockito.mock(Connection::class.java)
    val s = CachingSession(f)

    Mockito.doReturn(c1).doReturn(c2).`when`(f).create()

    s.acquire()

    Assert.assertNotNull(s.connections.get())

    s.release(c1)

    Assert.assertNull(s.connections.get())
    Assert.assertSame(c1, s.freeConnection)

    s.acquire()

    val that = this as Object
    synchronized(that) {
      val runnable = Runnable() {
        try {
          s.acquire()
        } finally {
          synchronized(that) {
            that.notify()

            that.wait()

            try {
              s.release(c2)
            } finally {
              that.notify()
            }
          }
        }
      }
      val t = Thread(runnable)
      t.isDaemon = true
      t.start()

      that.wait()
    }

    s.release(c1)
    Mockito.verify(c1, Mockito.never()).close()

    Assert.assertSame(c1, s.freeConnection)

    synchronized(that) {
      that.notify()

      that.wait()
    }

    Assert.assertSame(c1, s.freeConnection)
    Mockito.verify(c1, Mockito.never()).close()
    Mockito.verify(c2, Mockito.times(1)).close()
  }
}

