import sbt.internal.inc.{IfMissing, ZincComponentManager, ZincUtil}

val scala210    = "2.10.6"
val scala211    = "2.11.11"
val scala212    = "2.12.2"
val zincVersion = "1.0.0-X20"

val testVersions = Seq(scala210, scala211, scala212)
val Test210      = config("Test210")
val Test211      = config("Test211")
val Test212      = config("Test212")
val configs =
  Map(scala210 -> Test210, scala211 -> Test211, scala212 -> Test212)

addCommandAlias(
  "setupTests",
  // Compile all the required bridges
  Seq("project setupProject",
      "+ compile",
      // Compile the "test compiler" for all scala versions
      "project testCompiler",
      "+ compile",
      "project /").mkString(";", ";", "")
)

val sharedSettings = Seq(
  version := "0.7.0-SNAPSHOT",
  organization := "org.duhemm",
  scalaVersion := scala212,
  scalacOptions ++=
    Seq("-deprecation", "-feature", "-unchecked", "-Xlint")
)

bintrayReleaseOnPublish := false

lazy val errorsSummary =
  project
    .in(file("."))
    .settings(
      sharedSettings,
      name := "sbt-errors-summary",
      description := "sbt plugin to show a summary of compilation messages.",
      sbtPlugin := true,
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
      sharedSettings,
      crossScalaVersions := testVersions,
      libraryDependencies += "org.scala-sbt" % "compiler-interface" % zincVersion % Provided,
      libraryDependencies ++= compilerDependencies(scalaVersion.value,
                                                   Provided),
      // We need the compiled bridge on the classpath because `DelegatingReporter` moved from
      // compile-interface to the implementation of the bridge.
      unmanagedClasspath in Compile += {
        val ci = getCompilerInterface(
          appConfiguration.value,
          ZincUtil.getDefaultBridgeModule(scalaVersion.value),
          streams.value.log,
          scalaVersion.value)
        ci
      }
    )
    .dependsOn(testAPI)

lazy val testAPI =
  project
    .in(file("test-api"))
    .settings(
      sharedSettings,
      autoScalaLibrary := false,
      crossPaths := false
    )

// Project without dependencies whose only goal is to trigged compilation
// of all the missing bridges.
lazy val setupProject =
  project
    .in(file("setup-project"))
    .settings(
      sharedSettings,
      crossScalaVersions := testVersions
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
        val ci =
          getCompilerInterface(appConfiguration.value,
                               ZincUtil.getDefaultBridgeModule(scalaVersion),
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
