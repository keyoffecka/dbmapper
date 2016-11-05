package net.ofk.dbmapper.defaults.impl;

import java.sql.Connection
import java.util.HashMap
import java.util.regex.Pattern

/**
 * MySQL5 engine has its own variant name
 * and sets UTC time zone for every connection.
 */
class MySQL5Engine : BaseEngine(DefaultFormatterBuilder.build()) {
  //This is inherited from http://stackoverflow.com/a/6478616
  private val SEARCH_REGEX_REPLACEMENT = arrayOf(
    //search string     search regex        sql replacement regex
    arrayOf("\u0000"    ,       "\\x00"     ,       "\\\\0"     ),
    arrayOf("'"         ,       "'"         ,       "\\\\'"     ),
    arrayOf("\""        ,       "\""        ,       "\\\\\""    ),
    arrayOf("\b"        ,       "\\x08"     ,       "\\\\b"     ),
    arrayOf("\n"        ,       "\\n"       ,       "\\\\n"     ),
    arrayOf("\r"        ,       "\\r"       ,       "\\\\r"     ),
    arrayOf("\t"        ,       "\\t"       ,       "\\\\t"     ),
    arrayOf("\u001A"    ,       "\\x1A"     ,       "\\\\Z"     ),
    arrayOf("\\"        ,       "\\\\"      ,       "\\\\\\\\"  )
  )

  private val sqlTokensWithPattern = lazy { initSqlTokensAndPatterns() }

  override fun prepareConnection(conn: Connection) {
    val st = conn.createStatement()
    try {
      st.execute("set time_zone = '+00:00'")
    } finally {
      st.close()
    }
  }

  override fun variant() = "mysql"

  override fun escape(value: String): String {
    val sb = StringBuffer()

    val matcher = sqlTokensWithPattern.value.pattern.matcher(value)
    while(matcher.find()) {
      matcher.appendReplacement(sb, sqlTokensWithPattern.value.sqlTokens[matcher.group(1)])
    }
    matcher.appendTail(sb);

    return sb.toString();
  }

  private fun initSqlTokensAndPatterns(): SQLTokensWithPattern {
    var pattern = ""
    val sqlTokens = HashMap<String, String>()
    for (srr in SEARCH_REGEX_REPLACEMENT) {
      pattern+= (if(pattern.isEmpty()) "" else "|") + srr[1];
      sqlTokens+= srr[0] to srr[2]
    }
    return SQLTokensWithPattern(sqlTokens, Pattern.compile("($pattern)"))
  }

  private data class SQLTokensWithPattern(
    val sqlTokens: Map<String, String>,
    val pattern: Pattern
  )
}

