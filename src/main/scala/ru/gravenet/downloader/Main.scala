/**
 * Created by Maxim Zhukov <crmaxx@ya.ru> on 2015-01-30.
 */

package ru.gravenet.downloader

import akka.actor._
import org.rogach.scallop.Scallop
import org.rogach.scallop.exceptions.Help
import pl.project13.scala.rainbow._

object Main extends App with Utils {
  val startTime = System.nanoTime

  val config = new Config(args, onError = onError)

  val outputDir = getOutputDir(config.outputDir.get.get)

  val downloadList = parseDownloadList(config.fileList.get.get) match {
    case Some(list) => list
    case None =>
      Logger.printWarn("Nothing to download")
      sys.exit(0)
  }

  val numberOfThreads = config.numberOfThreads.get.get
  val limitRate = parseRateLimit(config.limitRate.get.get) match {
    case Some(i) => i
    case None =>
      Logger.printWarn("Wrong rate limit")
      sys.exit(0)
  }

  implicit val system = ActorSystem("downloader")
  val manager = system.actorOf(Props(new DownloadManager(downloadList, outputDir, numberOfThreads, limitRate)), "manager")

  // Await end of program
  system.awaitTermination()

  val elapsedTime = (System.nanoTime - startTime) / 1000000000.0
  Logger.printInfo(s"Elapsed time: $elapsedTime s")

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
