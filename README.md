# sbt-errors-summary

A simple plugin that makes the error reporter a bit more concise.

I find it useful when doing refactoring: I get a lot of compilation errors, and I waste a lot of
time switching between files and looking for line numbers in the error message, when I can
immediately see what's wrong when looking at the faulty line.

This plugin helps by summarizing all the errors per file.

```
[error] [0] /main/scala/Bar.scala:2: not found: value foobar
[error] [1] /main/scala/Foo.scala:4: type mismatch;
[error]      found   : String("")
[error]      required: Int
[warn] [2] /main/scala/Foo.scala:10: @deprecated now takes two arguments; see the scaladoc.
[warn] [3] /main/scala/Foo.scala:3: postfix operator hello should be enabled
[warn]     by making the implicit value scala.language.postfixOps visible.
[warn]     This can be achieved by adding the import clause 'import scala.language.postfixOps'
[warn]     or by setting the compiler option -language:postfixOps.
[warn]     See the Scala docs for value scala.language.postfixOps for a discussion
[warn]     why the feature should be explicitly enabled.
[error] /main/scala/Bar.scala: 2 [0]
[error] /main/scala/Foo.scala: 3 [3], 4 [1], 10 [2]
```

# Installation

Simply add the following to your sbt configuration:

```scala
resolvers += Resolver.bintrayIvyRepo("duhemm", "sbt-plugins")
addSbtPlugin("org.duhemm" % "sbt-errors-summary" % "0.2.0")
```

# Changelog

## 0.2.0
 - Apply `compilerReporter` to `Compile` and `Test` configurations,
   by @jvican and @Duhemm in #1 and #2.

## 0.1.0
 - Initial version
