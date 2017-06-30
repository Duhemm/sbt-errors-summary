lazy val errorsSummary = project.in(file("."))

version := "0.3.0-SNAPSHOT"
sbtPlugin := true
organization := "org.duhemm"
name := "sbt-errors-summary"
description := "sbt plugin to show a summary of compilation messages."
scalacOptions ++=
  Seq("-deprecation", "-feature", "-unchecked", "-Xlint", "-Ywarn-all")
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

libraryDependencies += "org.scalatest"  %% "scalatest"     % "3.0.1"            % Test
libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % Test
libraryDependencies += "org.scala-lang" % "scala-reflect"  % scalaVersion.value % Test

fullClasspath in Test := {
  val classpath = (fullClasspath in Test).value
  val testcp = classpath.files
    .map(_.getAbsolutePath)
    .mkString(java.io.File.pathSeparatorChar.toString)
  sys.props("sbt.class.directory") = testcp
  classpath
}
