package build

import sbt._
import sbt.internal.inc.{IfMissing, ZincComponentManager}

object CompilerUtils {

  def getCompilerInterface(app: xsbti.AppConfiguration,
                           sourcesModule: ModuleID,
                           log: Logger,
                           scalaVersion: String): File = {
    val launcher = app.provider.scalaProvider.launcher
    val componentManager = new ZincComponentManager(launcher.globalLock,
                                                    app.provider.components,
                                                    Option(launcher.ivyHome),
                                                    log)
    val binSeparator = "-bin_" // Keep in sync with `ZincComponentCompiler.binSeparator`
    val javaVersion  = sys.props("java.class.version")
    val id =
      s"${sourcesModule.organization}-${sourcesModule.name}-${sourcesModule.revision}${binSeparator}${scalaVersion}__${javaVersion}"
    componentManager.file(id)(IfMissing.Fail)
  }

  def compilerDependencies(scalaVersion: String,
                           config: Configuration): Seq[ModuleID] = Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion % config,
    "org.scala-lang" % "scala-reflect"  % scalaVersion % config,
    "org.scala-lang" % "scala-library"  % scalaVersion % config
  )
}
