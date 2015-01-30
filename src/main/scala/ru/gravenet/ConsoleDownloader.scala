/**
 * Created by mzhukov on 2015-01-30.
 */

import org.rogach.scallop.{ScallopConf, Scallop}
import org.rogach.scallop.exceptions.Help

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
  val limitSpeed = opt[String]()
  val fileList = opt[String](required = true)
  val outputDir = opt[String](required = true, short = 'o')

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

  println("numberOfThreads is %s".format(config.numberOfThreads))

  private def onError(e: Throwable, scallop: Scallop) = e match {
    case Help(_) =>
      scallop.printHelp
      sys.exit(0)
    case _ =>
      println("Error: %s\n".format(e.getMessage))
      scallop.printHelp
      sys.exit(1)
  }
}
