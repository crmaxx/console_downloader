/**
 * Created by mzhukov on 30/01/15.
 */

import org.rogach.scallop._

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  version("0.1 © 2015 crmaxx")
  banner("""Usage: java -jar console-downloader-0.1.jar [options]
           |Options:
           |""".stripMargin)
  footer("\nFor https://github.com/Ecwid/new-job/blob/master/Console-downloader.md")
  val numberOfThreads = opt[Int](required = true, default = Some(5), validate = (0<))
  val limitSpeed = opt[String]()
  val fileList = opt[String](required = true)
  val outputDir = opt[String](required = true, short = 'o')
}

/**
 * -n количество одновременно качающих потоков (1,2,3,4....)
 * -l общее ограничение на скорость скачивания, для всех потоков, размерность - байт/секунда, можно использовать суффиксы k,m (k=1024, m=1024*1024)
 * -f путь к файлу со списком ссылок
 * -o имя папки, куда складывать скачанные файлы
 */

object ConsoleDownloader {
  def main(args: Array[String]) = {
    val conf = new Conf(args)

    println("summary " + conf.summary)
  }
}
