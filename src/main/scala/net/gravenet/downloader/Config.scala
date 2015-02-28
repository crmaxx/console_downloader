/**
 * Created by Maxim Zhukov <crmaxx@ya.ru> on 2015-02-12.
 */

package net.gravenet.downloader

import org.rogach.scallop.{Scallop, ScallopConf}

class Config(arguments: Seq[String], onError: (Throwable, Scallop) => Nothing) extends ScallopConf(arguments) {
  version("ConsoleDownloader 0.1-alpha © 2015 crmaxx")
  banner("""
           | Usage: java -jar console-downloader-VERSION.jar [options]
           |
           | For https://github.com/Ecwid/new-job/blob/master/Console-downloader.md
           |
           |Options:
           |""".stripMargin)
  footer("\nMail bug reports and suggestions to <crmaxx@gmail.com>.")

  val numberOfThreads = opt[Int](required = true, default = Some(2), validate = (0<), descr = "count of concurrent downloads")
  val limitRate = opt[String](required = true, descr = "limit download rate to RATE.")
  val fileList = opt[String](required = true, descr = "download URLs found in local FILE.")
  val outputDir = opt[String](required = true, short = 'o', descr = "save files to DIR")

  override protected def onError(e: Throwable) = onError(e, builder)
}
