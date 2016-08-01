package net.ofk.dbmapper.defaults.impl

import java.text.DateFormat
import java.text.Format
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

/**
 * Creates instances of date formaters
 * used to format date parameters using MySQL-like syntax.
 */
object DefaultFormatterBuilder {
  private val FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  private val LOCALE = Locale.US;
  private val TZ = TimeZone.getTimeZone("UTC");

  fun build(): DateFormat {
    val f = SimpleDateFormat(FORMAT, LOCALE)
    f.calendar = GregorianCalendar(TZ, LOCALE)
    return f
  }
}