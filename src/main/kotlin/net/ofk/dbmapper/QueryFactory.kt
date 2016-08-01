package net.ofk.dbmapper

import net.ofk.kutils.Auto
import net.ofk.kutils.BaseInvocationHandler
import java.io.IOException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.text.MessageFormat
import java.util.Locale
import java.util.MissingResourceException
import java.util.Objects
import java.util.PropertyResourceBundle
import java.util.ResourceBundle

/**
 * Produces proxies for query interfaces.
 * Query interfaces define methods which return strings.
 * The returned values are taken from associated .properties files.
 * .properties files should have the same name as the name of the interface
 * and be located in the same package.
 * Defined methods may have parameters which will be passed as the parameters
 * of MessageFormat.format() method.
 * Names of the .properties files may have variant suffixes.
 * If a variant is passed to the factory method, the factory will try first
 * to get method return values from the .properties file named like
 * InterfaceSimpleClassName_variant.properties
 * If that file doesn't exit, has no appropriate value or variant wasn't passed
 * InterfaceSimpleClassName.properties is used.
 */
class QueryFactory {
  /**
   * Creates a proxy for the given interface.
   */
  fun <T> create(clazz: Class<T>, variant: String? = null): T
    = Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), QueryFactory.Handler(clazz, variant)) as T

  class Handler (private val clazz: Class<*>, private val variant: String?) : BaseInvocationHandler() {
    private val defaultBundle: Lazy<ResourceBundle> = lazy {
      createBundle("") ?: throw IOException("No default resource found for ${clazz.name}")
    }

    private val bundle: Lazy<ResourceBundle?> = lazy {
      if (variant == null || variant.isBlank()) {
        null
      } else {
        createBundle("_${variant.trim()}")
      }
    }

    private fun createBundle(suffix: String): PropertyResourceBundle? {
      val path = clazz.name.replace(".", "/") + "$suffix.properties"
      return Auto.close {
        val stream = clazz.classLoader.getResourceAsStream(path).open()
        if (stream == null) {
          null
        } else {
          PropertyResourceBundle(stream)
        }
      }
    }

    override fun doInvoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
      val b = bundle.value ?: defaultBundle.value
      val value = try {
        b.getString(method.name)
      } catch (ex: MissingResourceException){
        if (bundle.value == null) {
          method.name
        } else {
          try {
            defaultBundle.value.getString(method.name)
          } catch (ex: MissingResourceException) {
            method.name
          }
        }
      }
      return MessageFormat(value, Locale.US).format(args)
    }
  }
}
