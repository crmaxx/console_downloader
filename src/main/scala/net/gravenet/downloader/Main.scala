/**
 * Created by Maxim Zhukov <crmaxx@ya.ru> on 2015-01-30.
 */

package net.gravenet.downloader

import java.io._

import akka.actor._
import org.rogach.scallop.Scallop
import org.rogach.scallop.exceptions.Help
import pl.project13.scala.rainbow._

import scala.io.{Codec, Source}

object Main extends App {
  val startTime = System.nanoTime

  val config = new Config(args, onError = onError)

  val outputDir = getOutputDir(config.outputDir.get.get)
  val downloadList = parseDownloadList(config.fileList.get.get)
  val numberOfThreads = config.numberOfThreads.get.get
  val limitRate = parseRateLimit(config.limitRate.get.get)

  implicit val system = ActorSystem("downloader")
  val manager = system.actorOf(Props(new DownloadManager(downloadList, outputDir, numberOfThreads)), "manager")

  // Await end of program
  system.awaitTermination()

  val elapsedTime = (System.nanoTime - startTime) / 1000000000.0
  Logger.printInfo(s"Elapsed time: $elapsedTime s")

  /********************************************************************/

  def createDirectoryIfNeeded(directory: File): Unit = {
    if (!directory.exists) {
      Logger.printInfo("Creating directory: %s.".format(directory.getPath))
      directory.mkdir
    }
  }

  // TODO: Make lazy read downloadList from file with control unique links on download
  def readFileAsList(filePatch: String): List[String] = {
    val file = new File(filePatch)

    if (!file.exists && !file.canRead) {
      Logger.printError("Unable to read %s.".format(file.getName))
      sys.exit(1)
    }

    Source.fromFile(file)(Codec.UTF8).getLines().toList.filter(_.nonEmpty).distinct
  }

  def quitIfEmpty(downloadList: List[String]): Unit = {
    if (downloadList.isEmpty) {
      Logger.printError("No links for download.")
      sys.exit(1)
    }
  }

  def convertListToMap(downloadList: List[String]): Map[String,String] = {
    // TODO: case with ' ' in url
    val urls = downloadList.map(_.split(' ').head)
    val files = downloadList.map(_.split(' ').tail.head)
    urls.zip(files).toMap
  }

  def parseDownloadList(filePatch: String): Map[String,String] = {
    val downloadList = readFileAsList(filePatch)
    quitIfEmpty(downloadList)
    convertListToMap(downloadList)
  }

  def parseRateLimit(rateLimit: String): Int = {
    if (rateLimit.isEmpty) return -1
    val (value, postfix) = rateLimit.splitAt(rateLimit.length - 1)

    postfix match {
      case "k" => 1024 * value.toInt
      case "m" => 1024 * 1024 * value.toInt
      case _ => rateLimit.toInt
    }
  }

  def getOutputDir(output: String): String = {
    val outputDir = new File(config.outputDir.get.get)
    createDirectoryIfNeeded(outputDir)
    outputDir.getAbsolutePath
  }

  private def onError(e: Throwable, scallop: Scallop) = e match {
    case Help(_) =>
      scallop.printHelp()
      sys.exit(0)
    case _ =>
      println("Error: %s\n".format(e.getMessage).red)
      scallop.printHelp()
      sys.exit(1)
  }
}
