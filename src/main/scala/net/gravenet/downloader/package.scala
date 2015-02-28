/**
 * Created by Maxim Zhukov <crmaxx@ya.ru> on 2015-02-28.
 */

package net.gravenet

package object downloader {
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val executionContext = global

  case class Downloader(url: String, file: String, outputDir: String)
  case class DownloadResult(file: String, resp: Either[String, Int])
}
