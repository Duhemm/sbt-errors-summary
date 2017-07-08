val testVersions = Seq("2.10.6", "2.11.11", "2.12.2")
val configs = testVersions.map { v =>
  val configVersion = v.filterNot(_ == '.')
  v -> config(s"test$configVersion")
}.toMap

addCommandAlias(
  "setupTests",
  Seq("project testCompiler", "+ compile", "project /").mkString(";", ";", ""))

lazy val errorsSummary =
  project
    .in(file("."))
    .settings(
      version := "0.4.1",
      sbtPlugin := true,
      organization := "org.duhemm",
      name := "sbt-errors-summary",
      description := "sbt plugin to show a summary of compilation messages.",
      scalacOptions ++=
        Seq("-deprecation", "-feature", "-unchecked", "-Xlint", "-Ywarn-all"),
      licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      sourceManaged in (Compile, generateContrabands) := baseDirectory.value / "src" / "main" / "contraband-scala"
    )
    .settings(testVersions.flatMap(testSetup))
    .enablePlugins(ContrabandPlugin)
    .dependsOn(testAPI % Test)

lazy val testCompiler =
  project
    .in(file("test-compiler"))
    .settings(
      crossScalaVersions := testVersions,
      libraryDependencies += "org.scala-sbt" % "compiler-interface" % sbtVersion.value % Provided,
      libraryDependencies ++= compilerDependencies(scalaVersion.value,
                                                   Provided)
    )
    .dependsOn(testAPI)

lazy val testAPI =
  project
    .in(file("test-api"))
    .settings(
      autoScalaLibrary := false,
      crossPaths := false
    )

def testSetup(scalaVersion: String): Seq[Setting[_]] = {
  val shortVersion = scalaVersion.take(4)
  val testConfig   = configs(scalaVersion)
  inConfig(testConfig)(Defaults.testSettings) ++
    Seq(
      libraryDependencies ++= compilerDependencies(scalaVersion, testConfig),
      ivyConfigurations += testConfig,
      test in testConfig := {
        (test in Test).dependsOn(fullClasspath in testConfig).value
      },
      testOnly in testConfig := {
        (testOnly in Test).dependsOn(fullClasspath in testConfig).evaluated
      },
      fullClasspath in testConfig := {
        val ci = getCompilerInterface(appConfiguration.value,
                                      scalaCompilerBridgeSource.value,
                                      streams.value.log,
                                      scalaVersion)
        val compiler  = (target in testCompiler).value / s"scala-$shortVersion" / "classes"
        val classpath = (externalDependencyClasspath in testConfig).value
        val testcp = (classpath.files :+ compiler :+ ci)
          .map(_.getAbsolutePath)
          .mkString(java.io.File.pathSeparatorChar.toString)
        sys.props("test.compiler.cp") = testcp
        sys.props("test.scala.version") = scalaVersion
        classpath
      }
    )
}

def getCompilerInterface(app: xsbti.AppConfiguration,
                         sourcesModule: ModuleID,
                         log: Logger,
                         scalaVersion: String): File = {
  val launcher = app.provider.scalaProvider.launcher
  val componentManager = new ComponentManager(launcher.globalLock,
                                              app.provider.components,
                                              Option(launcher.ivyHome),
                                              log)
  val binSeparator = sbt.compiler.ComponentCompiler.binSeparator
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
