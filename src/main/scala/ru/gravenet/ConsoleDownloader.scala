/**
 * Created by Maxim Zhukov <crmaxx@ya.ru> on 2015-01-30.
 */

import org.rogach.scallop.exceptions.Help
import org.rogach.scallop.{Scallop, ScallopConf}
import pl.project13.scala.rainbow._

import scala.io.Source

class Config(arguments: Seq[String], onError: (Throwable, Scallop) => Nothing) extends ScallopConf(arguments) {
  version("ConsoleDownloader 0.1 © 2015 crmaxx")
  banner("""
           | Usage: java -jar console-downloader-0.1.jar [options]
           |
           | For https://github.com/Ecwid/new-job/blob/master/Console-downloader.md
           |
           |Options:
           |""".stripMargin)
  footer("\nMail bug reports and suggestions to <crmaxx@gmail.com>.")

  val numberOfThreads = opt[Int](required = true, default = Some(2), validate = (0<))
  val limitRate = opt[String](descr = "limit download rate to RATE.")
  val fileList = opt[String](required = true, descr = "download URLs found in local FILE.")
  val outputDir = opt[String](required = true, short = 'o', descr = "save files to DIR")

  override protected def onError(e: Throwable) = onError(e, builder)
}

/**
 * -n количество одновременно качающих потоков (1,2,3,4....)
 * -l общее ограничение на скорость скачивания, для всех потоков, размерность - байт/секунда, можно использовать суффиксы k,m (k=1024, m=1024*1024)
 * -f путь к файлу со списком ссылок
 * -o имя папки, куда складывать скачанные файлы
 */

object ConsoleDownloader extends App {
  val config = new Config(args, onError)

  printSuccess("numberOfThreads is %s".format(config.numberOfThreads.get.get))
  printWarn("limitRate is %s".format(config.limitRate.get.get))
  printInfo("fileList is %s".format(config.fileList.get.get))
  printError("outputDir is %s".format(config.outputDir.get.get))

  println("\n")

  val outputDir = new java.io.File(config.outputDir.get.get)
  createDirectoryIfNeeded(outputDir)

  val downloadList = readFileAsList(config.fileList.get.get)
  quitIfEmpty(downloadList)
  removeDuplicates(downloadList)
  printList(downloadList)

  /********************************************************************/

  def createDirectoryIfNeeded(directory: java.io.File): Unit = {
    if (!directory.exists) {
      printInfo("Creating directory: %s.".format(directory.getPath))
      directory.mkdir
    }
  }

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

  def removeDuplicates(downloadList: List[String]): Unit = {
    val links = downloadList.map(_.split(' ').head)
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
