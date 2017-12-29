package sbtstudent

/**
  * Copyright © 2014, 2015, 2016 Lightbend, Inc. All rights reserved. [http://www.typesafe.com]
  */

import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers._

import scala.Console
import scala.util.matching._

object Man {
  val manDetail: String = "Displays the README.md file. Use <noarg> for setup README.md or <e> for exercise README.md"

  lazy val optArg = OptSpace ~> StringBasic.?

  def man: Command = Command("man")(_ => optArg) { (state, arg) =>
    arg match {
      case Some(a) if a == "e" =>
        val base: File = Project.extract(state).get(sourceDirectory)
        val basePath: String = base + "/test/resources/README.md"
        printOut(basePath)
        Console.print("\n")
        state
      case Some(a) =>
        Console.print("\n")
        Console.println(Console.RED + "[ERROR] " + Console.RESET + "invalid argument " + Console.RED + a + Console.RESET  + ". " + manDetail)
        Console.print("\n")
        state
      case None =>
        val base: File = Project.extract(state).get(baseDirectory)
        val readMeFile = new sbt.File(new sbt.File(Project.extract(state).structure.root), "README.md")
        val basePath = readMeFile.getPath
        printOut(basePath)
        Console.print("\n")
        state
    }
  }

  val bulletRx: Regex = """- """.r
  val boldRx: Regex = """(\*\*)(\w*)(\*\*)""".r
  val codeRx: Regex = """(`)([^`]+)(`)""".r
  val fenceStartRx: Regex = """^```(bash|scala)$""".r
  val fenceEndRx: Regex = """^```$""".r
  val numberRx: Regex = """^(\d{1,3})(\. )""".r
  val urlRx: Regex = """(\()(htt[a-zA-Z0-9\-\.\/:]*)(\))""".r
  val ConBlue = Console.BLUE
  val ConGreen = Console.GREEN
  val ConMagenta = Console.MAGENTA
  val ConRed = Console.RED
  val ConReset = Console.RESET
  val ConYellow = Console.YELLOW

  def printOut(path: String) {
    var inCodeFence = false
    IO.readLines(new sbt.File(path)) foreach {
      case ln if !inCodeFence && ln.length > 0 && ln(0).equals('#') =>
        Console.println(ConRed + ln + ConReset)
      case ln if !inCodeFence && ln.matches(".*" + bulletRx.toString() + ".*") =>
        val lne = bulletRx replaceAllIn (ln, ConRed + bulletRx.toString() + ConReset)
        Console.println(rxFormat(rxFormat(rxFormat(lne, codeRx, ConGreen), boldRx, ConYellow), urlRx, ConMagenta,
          keepWrapper = true))
      case ln if !inCodeFence && ln.matches(numberRx.toString() + ".*") =>
        val lne = numberRx replaceAllIn (ln, _ match { case numberRx(n, s) => f"$ConRed$n$s$ConReset" })
        Console.println(rxFormat(rxFormat(lne, codeRx, ConGreen), boldRx, ConYellow))
      case ln if ln.matches(fenceStartRx.toString()) =>
        inCodeFence = true
        Console.print(ConGreen)
      case ln if ln.matches(fenceEndRx.toString()) =>
        inCodeFence = false
        Console.print(ConReset)
      case ln =>
        Console.println(rxFormat(rxFormat(rxFormat(ln, codeRx, ConGreen), boldRx, ConYellow), urlRx, ConMagenta,
          keepWrapper = true))
    }
  }

  def rxFormat(ln: String, rx: Regex, startColor: String, keepWrapper: Boolean = false): String = ln match {
    case `ln` if ln.matches(".*" + rx.toString + ".*") =>
      val lne = rx replaceAllIn (ln, _ match {
        case rx(start, in, stop) =>
          if (keepWrapper)
            f"$start$startColor$in$ConReset$stop"
          else
            f"$startColor$in$ConReset"
      })
      lne
    case _ =>
      ln
  }

}

