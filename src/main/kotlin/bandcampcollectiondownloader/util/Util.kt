package bandcampcollectiondownloader.util

import bandcampcollectiondownloader.core.BandCampDownloaderError
import bandcampcollectiondownloader.core.Constants
import java.util.*

class Util(private val logger: Logger) {

  fun isUnix(): Boolean {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    return os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0
  }

  fun isWindows(): Boolean {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    return os.indexOf("win") >= 0
  }

  fun replaceInvalidCharsByUnicode(s: String): String {
    var result: String = s
    for ((old, new) in Constants.UNICODE_CHARS_REPLACEMENTS) {
      result = result.replace(old, new)
    }
    return result
  }

  fun <T> retry(function: () -> T, retries: Int): T? {
    val attempts = retries + 1
    for (i in 1..attempts) {
      if (i > 1) {
        logger.log("Retrying (${i - 1}/${retries}).")
        Thread.sleep(1000)
      }
      try {
        return function()
      } catch (e: Throwable) {
        logger.log("""Error while trying: "${e.javaClass.name}: ${e.message}".""")
        logger.debug(e)
      }
    }
    val message = "Could not perform task after $retries retries."
    throw BandCampDownloaderError(message)
  }
}
