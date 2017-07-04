# sbt-errors-summary

A simple plugin that makes the error reporter a bit more concise.

I find it useful when doing refactoring: I get a lot of compilation errors, and I waste a lot of
time switching between files and looking for line numbers in the error message, when I can
immediately see what's wrong when looking at the faulty line.

This plugin helps by summarizing all the errors per file.

Side by side comparison (this plugin on the left, origin on the right):

![side-by-side.png](side-by-side.png)

```
[info] Compiling 2 Scala sources to /Users/martin/Desktop/foo/target/scala-2.12/classes...
[error] [1] /src/main/scala/Bar.scala:2:
[error]     type mismatch;
[error]      found   : String("")
[error]      required: Int
[error]       val x: Int = ""
[error]                    ^
[warn]  [2] /src/main/scala/Bar.scala:4:
[warn]      @deprecated now takes two arguments; see the scaladoc.
[warn]        @deprecated
[warn]         ^
[error] [3] /src/main/scala/Foo.scala:2:
[error]     not found: value foobar
[error]       def foo: String = foobar
[error]                         ^
[error] /src/main/scala/Bar.scala: 2 [1], 4 [2]
[error] /src/main/scala/Foo.scala: 2 [3]
[error] (compile:compileIncremental) Compilation failed
[error] Total time: 0 s, completed Jul 3, 2017 3:00:27 PM
```

# Installation

To enable this plugin globally, simply put the following in
`~/.sbt/0.13/plugins/plugins.sbt`:

```scala
addSbtPlugin("org.duhemm" % "sbt-errors-summary" % "0.3.0")
```

You can also enable it for a specific project by putting the same line in
`project/plugins.sbt` in your sbt project.

# Changelog

## 0.3.0
 - Show offending line again
 - Better alignment of messages
 - Write tests for the reporter, again multiple Scala versions in
   [#4](https://github.com/sbt-errors-summary/pull/4) and
   [#5](https://github.com/sbt-errors-summary/pull/5)
 - Address [feedback from discussion on Scala contributors](https://contributors.scala-lang.org/t/improving-the-compilation-error-reporting-of-sbt/935)
   in [#6](https://github.com/Duhemm/sbt-errors-summary/pull/6)
   - Start numbering problems at 1
   - Add a newline after file path
   - Highlight file name and line number
   - Disable colors and formatting in CI and Ensime
 - Relativize file paths from working directory in
   [#7](https://github.com/sbt-errors-summary/pull/7)

## 0.2.0
 - Apply `compilerReporter` to `Compile` and `Test` configurations, by
   [@jvican](https://github.com/jvican) and   [@Duhemm](https://github.com/Duhemm)
   in [#1](https://github.com/Duhemm/sbt-errors-summary/pull/1) and
   [#2](https://github.com/Duhemm/sbt-errors-summary/pull/2).

## 0.1.0
 - Initial version
