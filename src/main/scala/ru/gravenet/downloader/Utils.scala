/**
 * Created by Maxim Zhukov <crmaxx@ya.ru> on 2015-02-28.
 */

package ru.gravenet.downloader

import java.io.File

import scala.io.{Codec, Source}

trait Utils {
  def createDirectoryIfNeeded(directory: File): Unit = {
    if (!directory.exists) {
      Logger.printInfo("Creating directory: %s.".format(directory.getPath))
      try {
        directory.mkdir
      } catch {
        case  e : Throwable =>
          Logger.printError("Error on create new directory :%s".format(e.getMessage))
      }
    }
  }

  // TODO: Make lazy read downloadList from file with control unique links on download
  def readFileAsList(filePatch: String): Option[List[String]] = {
    try {
      val file = new File(filePatch)

      if (!file.exists && !file.canRead) {
        Logger.printError("Unable to read %s.".format(file.getName))
        sys.exit(1)
      }

      Some(Source.fromFile(file)(Codec.UTF8).getLines().toList.filter(_.nonEmpty).distinct)
    } catch {
      case  e : Throwable =>
        Logger.printError("Error on read download list file: %s".format(e.getMessage))
        None
    }
  }

  def quitIfEmpty(downloadList: List[String]): Unit = {
    if (downloadList.isEmpty) {
      Logger.printError("No links for download.")
      sys.exit(1)
    }
  }

  def convertListToMap(downloadList: List[String]): Option[Map[String,String]] = {
    // TODO: case with ' ' in url
    try {
      val urls = downloadList.map(_.split(' ').head)
      val files = downloadList.map(_.split(' ').tail.head)
      Some(urls.zip(files).toMap)
    } catch {
      case e : Throwable =>
        Logger.printError("Error on parse download list file: %s".format(e.getMessage))
        None
    }

  }

  def parseDownloadList(filePatch: String): Option[Map[String,String]] = {
    val downloadList = readFileAsList(filePatch)
    quitIfEmpty(downloadList.get)
    convertListToMap(downloadList.get)
  }

  def parseRateLimit(rateLimit: String): Option[Int] = {
    if (rateLimit.isEmpty) return None
    val (value, postfix) = rateLimit.splitAt(rateLimit.length - 1)

    try {
      postfix match {
        case "k" => Some(1024 * value.toInt)
        case "m" => Some(1024 * 1024 * value.toInt)
        case _ => Some(rateLimit.toInt)
      }
    } catch {
      case e : Throwable =>
        Logger.printError("Error on parse rate limit: %s".format(e.getMessage))
        None
    }
  }

  def getOutputDir(output: String): String = {
    val outputDir = new File(output)
    createDirectoryIfNeeded(outputDir)
    outputDir.getAbsolutePath
  }
}
