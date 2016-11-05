package net.ofk.dbmapper.defaults.impl

import net.ofk.dbmapper.defaults.api.Engine
import net.ofk.dbmapper.defaults.api.Session
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import java.sql.Connection

class DefaultTransactionTest {
  private val sess = Mockito.mock(Session::class.java)
  private val eng = Mockito.mock(Engine::class.java)
  private val tx = DefaultTransaction(sess, eng)

  @Test
  fun testExecute() {
    val conn = Mockito.mock(Connection::class.java)
    val result = Any()

    Mockito.doReturn(conn).`when`(sess).acquire()

    Assert.assertSame(result, tx.call { result })

    Mockito.verify(conn, Mockito.never()).rollback();
    Mockito.verify(conn).commit();
  }

  @Test
  fun testExecuteReleaseFailure() {
    val conn = Mockito.mock(Connection::class.java)
    val e = RuntimeException()

    Mockito.doReturn(conn).`when`(sess).acquire()
    Mockito.doThrow(e).`when`(sess).release(conn)

    try {
      tx.exec { }
    } catch(ex: Exception) {
      Assert.assertSame(e, ex)
    }

    Mockito.verify(conn, Mockito.never()).rollback();
    Mockito.verify(conn).commit();
  }

  @Test
  fun testExecuteAllFailure() {
    val conn = Mockito.mock(Connection::class.java)
    val x1 = RuntimeException()
    val x2 = RuntimeException()
    val x3 = RuntimeException()

    Mockito.doReturn(conn).`when`(sess).acquire()
    Mockito.doThrow(x1).`when`(conn).commit()
    Mockito.doThrow(x2).`when`(conn).rollback()
    Mockito.doThrow(x3).`when`(sess).invalidate(conn, x2)

    try {
      tx.exec { }
      Assert.fail()
    } catch(ex: Exception) {
      Assert.assertSame(x3, ex)
    }
  }

  @Test
  fun testExecuteFailure() {
    val conn = Mockito.mock(Connection::class.java)
    val e = RuntimeException()

    Mockito.doReturn(conn).`when`(sess).acquire()

    try {
      tx.exec { throw e }
      Assert.fail()
    } catch(ex: Exception) {
      Assert.assertSame(e, ex)
    }

    Mockito.verify(conn).rollback();
    Mockito.verify(conn, Mockito.never()).commit();
  }

  @Test
  fun testPrepareConnectionFailure() {
    val conn = Mockito.mock(Connection::class.java)
    val e1 = RuntimeException()
    val e2 = RuntimeException()

    Mockito.doReturn(conn).`when`(sess).acquire()
    Mockito.doThrow(e1).`when`(eng).prepareConnection(conn)
    Mockito.doThrow(e2).`when`(sess).invalidate(conn, e1)

    try {
      tx.exec { Assert.fail() }
      Assert.fail()
    } catch(ex: Exception) {
      Assert.assertSame(e2, ex)
    }

    Mockito.verify(conn, Mockito.never()).rollback();
    Mockito.verify(conn, Mockito.never()).commit();
  }
}