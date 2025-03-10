package bandcampcollectiondownloader.cli

import bandcampcollectiondownloader.core.BandcampCollectionDownloader
import bandcampcollectiondownloader.core.Constants
import bandcampcollectiondownloader.core.CookiesManagement
import bandcampcollectiondownloader.util.*
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable
import picocli.CommandLine

@CommandLine.Command(
    name = "java -jar bandcamp-collection-downloader.jar",
    description = ["Download items from an existing Bandcamp account collection."],
    mixinStandardHelpOptions = true,
    version = [Constants.VERSION])
class Command : Callable<Int> {

  @CommandLine.Parameters(
      arity = "1..1",
      description = ["The Bandcamp user account from which all releases must be downloaded."])
  var bandcampUser: String = ""

  @CommandLine.Option(
      names = ["--cookies-file", "-c"],
      required = false,
      description =
          [
              "A file containing valid Bandcamp credential cookies.",
              """The file must either be obtained using the Firefox extension "Cookie Quick Manager" (https://addons.mozilla.org/en-US/firefox/addon/cookie-quick-manager/) or using the Chrome extension "Get cookies.txt LOCALLY"
            (https://chrome.google.com/webstore/detail/get-cookiestxt-locally/cclelndahbckbenkjhflpdbgdldlbecc).""",
              "If no cookies file is provided, cookies from the local Firefox installation are used (Windows and Linux)."])
  var pathToCookiesFile: Path? = null

  @CommandLine.Option(
      names = ["--skip-hidden", "-s"],
      required = false,
      description = ["Don't download hidden items of the collection."])
  var skipHiddenItems: Boolean = false

  @CommandLine.Option(
      names = ["--audio-format", "-f"],
      required = false,
      description =
          [
              "The chosen audio format of the files to download (default: \${DEFAULT-VALUE}).",
              "Possible values: flac, wav, aac-hi, mp3-320, aiff-lossless, vorbis, mp3-v0, alac."])
  var audioFormat: String = "vorbis"

  @CommandLine.Option(
      names = ["--download-folder", "-d"],
      required = false,
      description =
          [
              "The folder in which downloaded releases must be extracted.",
              "The following structure is considered: <pathToDownloadFolder>/<artist>/<year> - <release>.",
              "(default: current folder)"])
  var pathToDownloadFolder: Path = Paths.get(".")

  @CommandLine.Option(
      names = ["-r", "--retries"],
      description = ["Amount of retries for each HTTP connection (default: 3)."])
  var retries: Int = 3

  @CommandLine.Option(
      names = ["-t", "--timeout"],
      description = ["Timeout in ms before giving up an HTTP connection (default: 50000)."])
  var timeout: Int = 50000

  @CommandLine.Option(
      names = ["-j", "--jobs"],
      description = ["Amount of parallel jobs (threads) to use (default: 4)."])
  var jobs: Int = 4

  @CommandLine.Option(
      names = ["-n", "--dry-run"],
      description = ["Perform a trial run with no changes made on the filesystem."])
  var dryRun: Boolean = false

  @CommandLine.Option(
      names = ["--debug"],
      description =
          ["Print extra debug information, including the complete java stack trace on error."])
  var debug: Boolean = false

  @CommandLine.Option(
      names = ["--no-covers-single-track"],
      description =
          [
              "Do not try to download covers for single-track releases. Useful when the ISP is blocking the Bandcamp server providing covers."])
  var noCoversSingleTrack: Boolean = false

  override fun call(): Int {
    val logger = Logger(this.debug)
    val io: IO = if (this.dryRun) DryIO(logger) else RealIO(logger)
    val util = Util(logger)
    val cookiesManagement = CookiesManagement(util)
    val app = BandcampCollectionDownloader(this, io, util, logger, cookiesManagement)
    app.downloadAll()
    return 0
  }
}
