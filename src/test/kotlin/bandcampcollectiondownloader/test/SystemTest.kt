package bandcampcollectiondownloader.test

import bandcampcollectiondownloader.cli.Command
import bandcampcollectiondownloader.core.BandCampDownloaderError
import bandcampcollectiondownloader.core.BandcampCollectionDownloader
import bandcampcollectiondownloader.core.CookiesManagement
import bandcampcollectiondownloader.util.DryIO
import bandcampcollectiondownloader.util.Logger
import bandcampcollectiondownloader.util.Util
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Note: bli is a valid bandcamp user (completely randomly chosen), but for which we have no
 * credentials (ie. valid cookies).
 */
class SystemTest {

  var logger = Logger(true)
  var util = Util(logger)
  var dryIO = DryIO(logger)
  var cookies = CookiesManagement(util)

  @BeforeEach
  fun prepare() {
    logger = Logger(true)
    util = Util(logger)
    dryIO = DryIO(logger)
    cookies = CookiesManagement(util)
  }

  private fun createDownloader(command: Command): BandcampCollectionDownloader {
    return BandcampCollectionDownloader(command, dryIO, util, logger, cookies)
  }

  @Test
  fun testErrorCookiesFileNotFound() {
    val command = Command()
    command.pathToCookiesFile = Paths.get("bli")
    assertThrows<BandCampDownloaderError> { createDownloader(command).downloadAll() }
  }

  //
  @Test
  fun testErrorCookiesFileInvalidJson() {
    val command = Command()
    command.pathToCookiesFile = Paths.get("./test-data/notjsoncookies.json")
    assertThrows<BandCampDownloaderError> { createDownloader(command).downloadAll() }
  }

  @Test
  fun testErrorCookiesFileInvalidContent_wrongkey() {
    val command = Command()
    command.pathToCookiesFile = Paths.get("./test-data/invalidcookies_wrongkeys.json")
    assertThrows<BandCampDownloaderError> { createDownloader(command).downloadAll() }
  }

  @Test
  fun testErrorCookiesFileInvalidContent_noarray() {
    val command = Command()
    command.pathToCookiesFile = Paths.get("./test-data/invalidcookies_noarray.json")
    assertThrows<BandCampDownloaderError> { createDownloader(command).downloadAll() }
  }

  @Test
  fun testErrorInvalidBandcampUser() {
    val command = Command()
    command.pathToCookiesFile = Paths.get("./test-data/wellformedcookies.json")
    command.bandcampUser = "zerz1e3687dfs3df7"
    command.timeout = 5000
    assertThrows<BandCampDownloaderError> { createDownloader(command).downloadAll() }
  }

  @Test
  fun testErrorCookiesUselessForBandcampUser() {
    val command = Command()
    command.pathToCookiesFile = Paths.get("./test-data/wellformedcookies.json")
    command.bandcampUser = "bli"
    command.timeout = 5000
    assertThrows<BandCampDownloaderError> { createDownloader(command).downloadAll() }
  }

  @Test
  fun testErrorNoCookiesAtAll() {
    addToEnv("HOME", "NOPE")
    val command = Command()
    command.pathToCookiesFile = null
    command.bandcampUser = "bli"
    command.timeout = 5000
    assertThrows<BandCampDownloaderError> { createDownloader(command).downloadAll() }
  }

  @Test
  fun testOKValidCookiesDryRun() {
    val command = Command()
    command.pathToCookiesFile = Paths.get("./test-data/bcdtestcookies.json")
    command.bandcampUser = "bcdtest"
    val tmpDir = Files.createTempDirectory("bandcamp-collection-downloader-test")
    tmpDir.toFile().deleteOnExit()
    command.pathToDownloadFolder = tmpDir
    command.dryRun = true
    createDownloader(command).downloadAll()
    assertFalse(dryIO.getUnzippedFiles().isEmpty())
  }

  @Throws(Exception::class)
  fun addToEnv(key: String, value: String) {
    val classes = Collections::class.java.declaredClasses
    val env = System.getenv()
    for (cl in classes) {
      if ("java.util.Collections\$UnmodifiableMap" == cl.name) {
        val field = cl.getDeclaredField("m")
        field.isAccessible = true
        val obj = field.get(env)

        @Suppress("UNCHECKED_CAST") val map = obj as MutableMap<String, String>
        map[key] = value
      }
    }
  }
}
