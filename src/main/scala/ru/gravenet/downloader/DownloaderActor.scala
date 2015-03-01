/**
 * Created by Maxim Zhukov <crmaxx@ya.ru> on 2015-02-15.
 */

package ru.gravenet.downloader

import java.io._
import java.net.{HttpURLConnection, URL}
import com.google.common.util.concurrent.RateLimiter
import akka.actor.Actor

class DownloaderActor extends Actor {
  def receive = {
    case Download(url, file, outputDir, bytesPerSecond) =>
      val res = download(url, file, outputDir, bytesPerSecond)
      sender ! DownloadResult(file, res)
    case msg =>
      Logger.printError(s"DownloaderActor: Something wrong. $msg")
  }

  private def download(link: String, file: String, outputDir: String, bytesPerSecond: Double): Either[String, Long] = {
    val targetFilePath = s"$outputDir/$file"
    val rateLimiter = RateLimiter.create(bytesPerSecond)

    var in = None: Option[InputStream]
    var out = None: Option[OutputStream]

    try {
      val url = new URL(link)
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]

      connection.setRequestMethod("GET")
      connection.setRequestProperty("Accept", "*/*")
      connection.setRequestProperty("Accept-Encoding", "identity")
      connection.setRequestProperty("User-Agent", "ConsoleDownloader/0.1-alpha (Scala %s)".format(util.Properties.versionNumberString))

      in = Some(connection.getInputStream)

      val stream = new FileOutputStream(targetFilePath)
      out = Some(new BufferedOutputStream(stream))

      var c = 0
      while ({ c = in.get.read; c != -1 }) {
        rateLimiter.acquire()
        out.get.write(c)
      }

      val outFileSize = new File(targetFilePath).length()

      Right(outFileSize)
    } catch {
      case e: Throwable =>
        Left(e.getMessage)
    } finally {
      if (in.isDefined) in.get.close()
      if (out.isDefined) out.get.close()
    }
  }
}
