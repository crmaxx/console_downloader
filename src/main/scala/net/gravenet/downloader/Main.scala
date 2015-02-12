/**
 * Created by Maxim Zhukov <crmaxx@ya.ru> on 2015-01-30.
 */
package net.gravenet.downloader

import java.util.concurrent.Executors
import org.rogach.scallop.Scallop
import org.rogach.scallop.exceptions.Help
import pl.project13.scala.rainbow._

import scala.concurrent.ExecutionContext
import scala.io.Source

object Main extends App {
  val config = new Config(args, onError = onError)

  val outputDir = getOutputDir(config.outputDir.get.get)
  val downloadList = parseDownloadList(config.fileList.get.get)
  val numberOfThreads = config.numberOfThreads.get.get
  val limitRate = parseRateLimit(config.limitRate.get.get)

  implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(numberOfThreads))



  println("\n")
  printSuccess("numberOfThreads is %s".format(numberOfThreads))
  printWarn("limitRate is %s".format(limitRate))
  printInfo("fileList is %s".format(config.fileList.get.get))
  printError("outputDir is %s".format(outputDir.getPath))
  printError("downloadList is %s".format(downloadList))

  /********************************************************************/

  def createDirectoryIfNeeded(directory: java.io.File): Unit = {
    if (!directory.exists) {
      printInfo("Creating directory: %s.".format(directory.getPath))
      directory.mkdir
    }
  }

  // TODO: Make lazy read downloadList from file with control unique links on download
  def readFileAsList(filePatch: String): List[String] = {
    val file = new java.io.File(filePatch)

    if (!file.exists && !file.canRead) {
      printError("Unable to read %s.".format(file.getName))
      sys.exit(1)
    }

    Source.fromFile(file, "UTF-8").getLines.toList.filter(_.nonEmpty).distinct
  }

  def quitIfEmpty(downloadList: List[String]): Unit = {
    if (downloadList.isEmpty) {
      printError("No links for download.")
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
    printList(downloadList) // for debug
    convertListToMap(downloadList)
  }

  def parseRateLimit(rateLimit: String): Int = {
    if (rateLimit.isEmpty) return -1
    val (value, postfix) = rateLimit.splitAt(rateLimit.length - 1)
    val limit = postfix match {
      case "k" => 1024 * value.toInt
      case "m" => 1024 * 1024 * value.toInt
      case _ => rateLimit.toInt
    }
    limit
  }

  def getOutputDir(output: String): java.io.File = {
    val outputDir = new java.io.File(config.outputDir.get.get)
    createDirectoryIfNeeded(outputDir)
    outputDir
  }

  /********************************************************************/

  def printList(args: List[String]) = args.foreach(printSuccess)

  def printInfo(msg: String): Unit = {
    println { "[info] " + msg }
  }

  def printSuccess(msg: String): Unit = {
    println { "[" + "success".green + "] " + msg }
  }

  def printWarn(msg: String): Unit = {
    println { "[" + "warn".yellow + "] " + msg }
  }

  def printError(msg: String): Unit = {
    println { "[" + "error".red + "] " + msg }
  }

  /********************************************************************/

  private def onError(e: Throwable, scallop: Scallop) = e match {
    case Help(_) =>
      scallop.printHelp
      sys.exit(0)
    case _ =>
      println("Error: %s\n".format(e.getMessage).red)
      scallop.printHelp
      sys.exit(1)
  }
}
