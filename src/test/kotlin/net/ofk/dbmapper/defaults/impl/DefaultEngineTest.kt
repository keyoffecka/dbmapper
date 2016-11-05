package net.ofk.dbmapper.defaults.impl

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

class DefaultEngineTest {
  private var e: DefaultEngine? = null

  @Before
  fun setUp() {
    e = DefaultEngine()
  }

  @Test
  fun test() {
    val cal = GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US)
    cal.timeInMillis = 2

    Assert.assertEquals("'x'", e!!.buildQuery(":a", "a", "x"))
    Assert.assertEquals("'y', 'x', null", e!!.buildQuery(":a, :b, :c", "b", "x", "a", "y", "c", null))
    Assert.assertEquals("11,1,'1970-01-01 00:00:00.001','1970-01-01 00:00:00.002'", e!!.buildQuery(":a", "a", arrayOf(11, true, Date(1), cal)))
    Assert.assertEquals("1,0,null,'1970-01-01 00:00:00.002','1970-01-01 00:00:00.001'", e!!.buildQuery(":a", "a", listOf(1, false, null, cal, Date(1))))

    try {
      e!!.buildQuery(":a", "a", Any())
      Assert.fail()
    } catch(e: IllegalArgumentException) {
      Assert.assertEquals("Unsupported type java.lang.Object", e.message)
    }

    try {
      e!!.buildQuery(":a, :b")
      Assert.fail()
    } catch(e: Exception) {
      Assert.assertEquals("Please pass the missing parameter `a': :a, :b", e.message)
    }

    try {
      e!!.buildQuery(":a, :b", "a", "x")
      Assert.fail()
    } catch(e: Exception) {
      Assert.assertEquals("Please pass the missing parameter `b': :a, :b", e.message)
    }
  }

  @Test
  fun testEscape() {
    Assert.assertEquals("abcd", e!!.escape("abcd"))
    Assert.assertEquals("''ab''''cd''", e!!.escape("'ab''cd'"))
  }
}