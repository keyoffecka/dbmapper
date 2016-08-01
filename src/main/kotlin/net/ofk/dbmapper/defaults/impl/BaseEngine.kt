package net.ofk.dbmapper.defaults.impl

import net.ofk.dbmapper.defaults.api.Engine
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.HashMap

/**
 * Prepares SQL-queries using default data converters.
 */
abstract class BaseEngine(private val dateFormatter: DateFormat) : Engine {
  companion object {
    private val LOG = LoggerFactory.getLogger(Engine::class.java)
    private val PARAM_START = ':'
    private val PARAM_ENDS = " ,\r\n()\"'`=!".toCharArray()
  }

  override fun buildQuery(pattern: String, vararg paramValues: Any?): String {
    val map = HashMap<String, Any?>();

    var query = "";
    var rest = pattern;
    while (!rest.isEmpty()) {
      val start = rest.indexOf(PARAM_START);
      if (start == -1) {
        query+= rest;
        rest = "";
      } else {
        query+= rest.substring(0, start);

        var end = -1;
        for (ch in PARAM_ENDS) {
          val i = rest.indexOf(ch, start + 1);
          if (i != -1 && (end == -1 || i < end)) {
            end = i;
          }
        }
        if (end == -1) {
          end = rest.length
        }

        val param = rest.substring(start + 1, end);
        rest = rest.substring(end);

        var value = map[param];
        if (value == null) {
          var found = false;
          for (i in 0..paramValues.size - 1 step 2) {
            found = param == paramValues[i];
            if (found) {
              value = paramValues[i + 1];
              break;
            }
          }
          if (!found) {
            throw IllegalStateException("Please pass the missing parameter `$param': $pattern");
          }

          map.put(param, value);
        }

        query+= convert(value);
      }
    }

    LOG.trace("Built query: " + query);

    return query;
  }

  private fun convert(value: Any?): String =
    if (value == null) {
      "" + null
    } else {
      if (value.javaClass.isArray) {
        this.convert((value as Array<*>).asIterable());
      } else if (value is Iterable<*>) {
        this.convert(value.iterator())
      } else if (value is Iterator<*>) {
        var r = ""
        value.forEach { v ->
          if (!r.isEmpty()) {
            r+= ","
          }
          r+= convert(v)
        }
        r
      } else if (value is Calendar) {
        "'${this.dateFormatter.format(value.time)}'"
      } else if (value is Date) {
        "'${this.dateFormatter.format(value)}'"
      } else if (value is String) {
        "'$value'"
      } else if (value is Number){
        value.toString()
      } else if (value is Boolean){
        if (value) {
          "1"
        } else {
          "0"
        }
      } else {
        throw IllegalArgumentException("Unsupported type ${value.javaClass.name}");
      }
    }
}