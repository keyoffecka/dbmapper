package net.ofk.dbmapper.defaults.impl

import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import java.sql.Connection
import java.sql.Statement

class MySQL5EngineTest {
  @Test
  fun test() {
    val conn = Mockito.mock(Connection::class.java)
    val st = Mockito.mock(Statement::class.java)
    val eng = MySQL5Engine()
    val e = RuntimeException()

    Mockito.doReturn(st).`when`(conn).createStatement()

    eng.prepareConnection(conn)

    Assert.assertEquals("mysql", eng.variant())

    var o = Mockito.inOrder(st)
    o.verify(st).execute("set time_zone = '+00:00'")
    o.verify(st).close()

    Mockito.doThrow(e).`when`(st).execute("set time_zone = '+00:00'")

    try {
      eng.prepareConnection(conn)
    } catch(ex: Exception) {
      Assert.assertSame(e, ex)
    }

    o = Mockito.inOrder(st)
    o.verify(st).close()
  }

  @Test
  fun testEscape() {
    val eng = MySQL5Engine()

    Assert.assertEquals("abcd", eng.escape("abcd"))
    Assert.assertEquals("\\'\\\\ab\\'\\'cd\\'\\Z\\t\\r\\n\\b\\\"\\0", eng.escape("'\\ab''cd'\u001A\t\r\n\b\"\u0000"))
  }
}