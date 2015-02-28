/**
 * Created by Maxim Zhukov <crmaxx@ya.ru> on 2015-02-15.
 */

package net.gravenet.downloader

import java.io._
import java.net.{HttpURLConnection, URL}

import akka.actor.Actor

class DownloaderActor extends Actor {
  def receive = {
    case Downloader(url, file, outputDir) =>
      val res = download(url, file, outputDir)
      sender ! DownloadResult(file, res)
    case msg =>
      Logger.printError(s"DownloaderActor: Something wrong. $msg")
  }

  // TODO: chunked file download with download speed limit
  private def download(link: String, file: String, outputDir: String): Either[String, Int] = {
    try {
      val url = new URL(link)
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]

      connection.setRequestMethod("GET")
      connection.setRequestProperty("Accept", "*/*")
      connection.setRequestProperty("Accept-Encoding", "identity")
      connection.setRequestProperty("User-Agent", "Wget/1.16.1 (darwin14.0.0)")

      val in: InputStream = connection.getInputStream
      val stream = new FileOutputStream(file)
      val out: OutputStream = new BufferedOutputStream(stream)
      val byteArray = Stream.continually(in.read).takeWhile(_ != -1).map(_.toByte).toArray

      out.write(byteArray)
      out.close()
      in.close()

      Right(byteArray.size)
    } catch {
      case e: Throwable =>
        Left(e.getMessage)
    }
  }
}
