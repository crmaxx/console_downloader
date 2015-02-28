/**
 * Created by Maxim Zhukov <crmaxx@ya.ru> on 2015-02-28.
 */

package net.gravenet.downloader

import akka.actor.{Props, Actor}

class DownloadManager(downloadList: Map[String, String], outputDir: String, numberOfThreads: Int, limitRate: Int) extends Actor {
  // TODO: brainstorming about remove var
  var processedBytes: Long = 0
  var downloadListSize = downloadList.size
  var processedLinks = numberOfThreads
  val bytesPerSecond = limitRate / numberOfThreads

  downloadList.take(numberOfThreads).map {
    case (url, file) =>
      downloader(url, file, outputDir, bytesPerSecond)
  }

  def receive = {
    case DownloadResult(file, resp) =>
      resp match {
        case Right(size) =>
          Logger.printSuccess(s"Downloaded $file, size = $size bytes")
          processedBytes += size
        case Left(errorMsg) =>
          Logger.printError(s"On download $file, errorMsg = $errorMsg ")
      }

      downloadListSize -= 1

      if (downloadListSize == 0) {
        Logger.printInfo(s"Total downloaded $processedBytes bytes.")
        context.system.shutdown()
      } else {
        downloadList.drop(processedLinks).take(1).map {
          case (url, fileName) =>
            downloader(url, fileName, outputDir, bytesPerSecond)
        }
        processedLinks += 1
      }
    case msg =>
      Logger.printError(s"DownloadManager: Something wrong. $msg")
  }

  private def downloader(url: String, file: String, outputDir: String, bytesPerSecond: Double): Unit = {
    val downloader = context.actorOf(Props(new DownloaderActor))
    Logger.printInfo(s"Start download $url to $outputDir/$file")
    downloader ! Download(url, file, outputDir, bytesPerSecond)
  }
}
