package migrate

import migrate.interfaces.Lib
import scala.io.AnsiColor._

object Messages {
  def welcomeMigration(projectD: String): String =
    s"We are going to migrate your project $projectD to ${ScalaMigratePlugin.scala3Version}"
  def welcomePrepareMigration(projectD: String): String =
    s"We are going to fix some syntax incompatibilities on $projectD"
  def notScala213(scalaVersion: String, projectId: String) =
    s"""
       |
       |Error:
       |
       |you project must be in 2.13 and not in $scalaVersion
       |please change the scalaVersion following this command
       |set $projectId / scalaVersion := "2.13.5"
       |
       |
       |""".stripMargin

  def successOfMigration(projectId: String, scala3: String): String =
    s"""|
        |
        |$projectId project has successfully been migrated to scala $scala3
        |You can now commit the change!
        |You can also execute the compile command:
        |
        |$projectId / compile
        |
        |
        |""".stripMargin

  def errorMesssageMigration() =
    s"""|
        |
        |Migration has failed!
        |
        |
        |""".stripMargin

  def successMessagePrepareMigration(projectId: String, scala3: String) =
    s"""|
        |
        |The syntax incompatibilities have been fixed on the project $projectId
        |You can now commit the change!
        |You can also execute the next command to try to migrate to $scala3
        |
        |migrate $projectId
        |
        |
        |""".stripMargin

  def errorMessagePrepareMigration(projectId: String, ex: Throwable) =
    s"""|
        |
        |Failed fixing the syntax for $projectId project
        |${ex.getMessage()}
        |
        |
        |""".stripMargin

  def migrationScalacOptionsStarting(projectId: String) =
    s"""|
        |${BOLD}Starting to migrate the scalacOptions for $projectId${RESET}
        |""".stripMargin

  val warnMessageScalacOption: String =
    s"""|${YELLOW}Some scalacOptions are set by plugins and don't need to be modified, removed or added.${RESET}
        |${YELLOW}The plugin will adapt its own scalacOptions for Scala 3${RESET}""".stripMargin

  def notParsed(s: Seq[String]): Option[String] =
    if (s.nonEmpty)
      Some(s"""|
               |We were not able to parse the following ScalacOptions:
               |${formatScalacOptions(s)}
               |
               |""".stripMargin)
    else None

  def scalacOptionsMessage(removed: Seq[String], renamed: Map[String, String], scala3cOptions: Seq[String]): String = {
    val removedSign           = s"""${BOLD}${RED}X${RESET}"""
    val sameSign              = s"""${BOLD}${CYAN}\u2714${RESET}"""
    val renamedSign           = s"""${BOLD}${BLUE}Renamed${RESET}"""
    def formatRemoved: String = removed.map(r => s""""$r" -> $removedSign""").mkString("\n")
    def formatRenamed: String = renamed.map { case (initial, renamed) =>
      s""""$initial" -> ${BOLD}${BLUE}"$renamed"${BLUE}"""
    }.mkString("\n")
    def formatScala3cOptions: String = scala3cOptions.map(r => s""""$r" -> $sameSign""").mkString("\n")

    s"""
       |
       |$removedSign         $RED: The following scalacOption is specific to Scala 2 and doesn't have an equivalent in Scala 3$RESET
       |$renamedSign $BLUE: The following scalacOption has been renamed in Scala3$RESET
       |$sameSign         $CYAN: The following scalacOption is a valid Scala 3 option$RESET
       |
       |
       |$formatRemoved
       |$formatRenamed
       |$formatScala3cOptions
       |
       |
       |""".stripMargin
  }

  def migrateLibsStarting(projectId: String): String =
    s"""|
        |
        |Starting to migrate libDependencies for $projectId
        |""".stripMargin

  def notMigratedLibs(libs: Seq[Lib]): String = {
    val (compilerPlugins, others) = libs.partition(_.isCompilerPlugin)
    val messageCompilerPlugin = if (compilerPlugins.nonEmpty) {
      s"""|
          |The following compiler plugins are not supported in scala ${ScalaMigratePlugin.scala3Version}
          |You need to find alternatives. Please check the migration guide for more information.
          |${compilerPlugins.map(_.toString).mkString("\n")}
          |""".stripMargin
    } else ""
    val messageOtherLibs =
      if (others.nonEmpty)
        s"""
           |The following list of libs cannot be migrated as they contain Macros and are not yet
           |published for ${ScalaMigratePlugin.scala3Version}
           |
           |${others.map(_.toString).mkString("\n")}
           |
           |""".stripMargin
      else ""
    messageCompilerPlugin + messageOtherLibs
  }

  def compilerPluginWithScalacOption(plugins: Map[Lib, String]): String =
    s"""
       |The following compiler plugins are not supported in scala ${ScalaMigratePlugin.scala3Version}
       |but there is an equivalent scalacOption that can replace it.
       |Add these scalacOptions to your ScalacOptions:
       |
       |${formatCompilerPlugins(plugins)}
       |
       |""".stripMargin

  def migratedLib(libs: Map[Lib, Seq[Lib]]): String =
    s"""|
        |
        |You can update your libs with the following versions: 
        |
        |${formatLibs(libs)}
        |
        |
        |
        |""".stripMargin

  private def formatCompilerPlugins(l: Map[Lib, String]): String =
    l.map { case (l, scalacOption) => format(l, Seq(scalacOption)) }.mkString("\n")

  private def formatLibs(libs: Map[Lib, Seq[Lib]]): String =
    libs.map { case (initial, migrated) => format(initial, migrated.map(_.toString)) }.mkString("\n")

  private def format(initial: Lib, migrated: Seq[String]): String =
    s"""\"$initial\" -> ${migrated.mkString(", ")}"""

  private def formatScalacOptions(l: Seq[String]): String =
    l.mkString("Seq(\n\"", "\",\n\"", "\"\n)")

}
