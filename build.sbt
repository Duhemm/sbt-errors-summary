import build.CompilerUtils
import sbt.internal.inc.ZincUtil

val scala210    = "2.10.6"
val scala211    = "2.11.12"
val scala212    = "2.12.12"
val zincVersion = "1.2.4"

val testVersions = Seq(scala210, scala211, scala212)
val Test210      = config("Test210")
val Test211      = config("Test211")
val Test212      = config("Test212")
val configs =
  Map(scala210 -> Test210, scala211 -> Test211, scala212 -> Test212)

addCommandAlias(
  "setupTests",
  // Compile all the required bridges
  Seq(
    "project setupProject",
    "+ compile",
    // Compile the "test compiler" for all scala versions
    "project testCompiler",
    "+ compile",
    "project /"
  ).mkString(";", ";", "")
)

inThisBuild(
  List(
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    // These are normal sbt settings to configure for release, skip if already defined
    homepage := Some(url("https://github.com/Duhemm/sbt-errors-summary")),
    developers := List(
      Developer(
        "@Duhemm",
        "Martin Duhem",
        "martin.duhem@gmail.com",
        url("https://github.com/Duhemm")
      )
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/Duhemm/sbt-errors-summary"),
        "scm:git:git@github.com:Duhemm/sbt-errors-summary.git"
      )
    ),
    scalafixCaching := true,
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.3.1-RC3",
    crossScalaVersions := List(scala212),
    publish / skip := true,
    organization := "com.github.duhemm",
    sonatypeProfileName := organization.value
  )
)

def on212Only[T](scalaBinaryVersion: String, if212: => T, otherwise: => T) =
  if (scalaBinaryVersion == "2.12") if212
  else otherwise

val sharedSettings = Seq(
  scalaVersion := scala212,
  scalacOptions ++=
    Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
  scalacOptions ++= on212Only(
    scalaBinaryVersion.value,
    Seq("-Yrangepos", "-Ywarn-unused"),
    Nil
  ),
  libraryDependencies ++= on212Only(
    scalaBinaryVersion.value,
    List(compilerPlugin(scalafixSemanticdb)),
    Nil
  )
)

lazy val errorsSummary =
  project
    .in(file("sbt-errors-summary"))
    .settings(
      sharedSettings,
      publish / skip := false,
      name := "sbt-errors-summary",
      description := "sbt plugin to show a summary of compilation messages.",
      sbtPlugin := true,
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      sourceManaged in (Compile, generateContrabands) := baseDirectory.value / "src" / "main" /
        "contraband-scala"
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
      libraryDependencies ++= CompilerUtils
        .compilerDependencies(scalaVersion.value, Provided),
      // We need the compiled bridge on the classpath because `DelegatingReporter` moved from
      // compile-interface to the implementation of the bridge.
      unmanagedClasspath in Compile += {
        val ci = CompilerUtils.getCompilerInterface(
          appConfiguration.value,
          ZincUtil.getDefaultBridgeModule(scalaVersion.value),
          streams.value.log,
          scalaVersion.value
        )
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
      libraryDependencies ++= CompilerUtils
        .compilerDependencies(scalaVersion, testConfig),
      ivyConfigurations += testConfig,
      test in testConfig := {
        (test in Test).dependsOn(fullClasspath in testConfig).value
      },
      testOnly in testConfig := {
        (testOnly in Test).dependsOn(fullClasspath in testConfig).evaluated
      },
      fullClasspath in testConfig := {
        val ci =
          CompilerUtils.getCompilerInterface(
            appConfiguration.value,
            ZincUtil.getDefaultBridgeModule(scalaVersion),
            streams.value.log,
            scalaVersion
          )
        val compiler =
          (target in testCompiler).value / s"scala-$shortVersion" / "classes"
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
