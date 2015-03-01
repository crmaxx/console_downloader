/**
 * Created by Maxim Zhukov <crmaxx@ya.ru> on 2015-02-15.
 */

package ru.gravenet.downloader

import pl.project13.scala.rainbow._

object Logger {
  def printList(args: TraversableOnce[_]) = args.foreach(printSuccess)

  def printInfo(msg: Any): Unit = {
    println { "[info] " + msg }
  }

  def printSuccess(msg: Any): Unit = {
    println { "[" + "success".green + "] " + msg }
  }

  def printWarn(msg: Any): Unit = {
    println { "[" + "warn".yellow + "] " + msg }
  }

  def printError(msg: Any): Unit = {
    println { "[" + "error".red + "] " + msg }
  }
}
