package sbtstudent

import sbt.Keys._
import sbt._
import stbstudent.MPSelection

import scala.Console

object StudentCommandsPlugin extends AutoPlugin {

  override val requires = sbt.plugins.JvmPlugin
  override val trigger: PluginTrigger = allRequirements
  object autoImport {
  }
  override lazy val globalSettings =
    Seq(
      commands in Global ++=
        Seq(
          Man.man, MPSelection.activateAllExercises, MPSelection.setActiveExerciseNr
        ),
      onLoad in Global := {
        val state = (onLoad in Global).value
        Navigation.loadBookmark compose(Navigation.setupNavAttrs compose state)
      }
    )

  override lazy val projectSettings =
    Seq(
      shellPrompt := { state =>
        val base: File = Project.extract(state).get(sourceDirectory)
        val basePath: String = base + "/test/resources/README.md"
        val exercise = Console.GREEN + IO.readLines(new sbt.File(basePath)).head + Console.RESET
        val manRmnd = Console.GREEN + "man [e]" + Console.RESET
        val prjNbrNme = IO.readLines(new sbt.File(new sbt.File(Project.extract(state).structure.root), ".courseName")).head
        s"$manRmnd > $prjNbrNme > $exercise > "
      }
    )
}
