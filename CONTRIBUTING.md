# Contributing

## Building the plugin

sbt-errors-summary needs sbt to be build. Starting `sbt` in this repository should be sufficient.
Type `compile` in the sbt shell to build the plugin.

## Project structure

sbt-errors-summary consists of 4 modules:

```
sbt:sbt-errors-summary> projects
[info] In file:/Users/martin/Documents/Projects.nosync/Duhemm/sbt-errors-summary/
[info]   * errorsSummary
[info]     setupProject
[info]     testAPI
[info]     testCompiler
```

### `errorsSummary`

This module contains the sbt plugin. It is built against sbt 1.0 only (sbt 0.13 support was
dropped after `v0.6.0`). For running the tests, it depends on the `testAPI` project.

This is the only module that is published.

### `setupProject`

This module is not connected to any other module in the dependency graph. It is cross-built
against all the versions of Scala for which we want to test `sbt-errors-summary`: the goal is
to ensure that we have precompiled compiler bridges for all the Scala versions that we will
use in the tests.

### `testAPI`

`testAPI` is a pure Java project that only defines the API of the test compiler that is used
for testing.

### `testCompiler`

This module defines the class `cross.Compiler`, which implements the API defined in the module
`testAPI`. This compiler receives a `Reporter` (that will receive the error messages) and a
`String` (representing the code to compile), and will try to compile it. A test can then look
into the reporter to verify that its output matches the expectations.

## Running the tests

The `testCompiler` needs to have the compiler bridge on the classpath. To ensure that we
have a compiler bridge that is ready to use for the Scala version that will run the tests, we
cross build `setupProject` using `;project setupProject ;+compile`.

The actual tests live in the `errorsSummary` module. For every Scala version that we want to
test, there is a different configuration: `Test210`, `Test211` and `Test212`. Each of these
configuration needs the corresponding `testCompiler` on its classpath, so we have to
cross-build the `testCompiler` against these versions using: `;project testCompiler;
+compile`.

All these setup steps can be performed using the alias: `setupTests`.

Once this is done, the tests for a given Scala version can be executed with:

 - `Test210/test` to test against Scala 2.10
 - `Test211/test` to test against Scala 2.11
 - `Test212/test` to test against Scala 2.12

## Releasing a new version

New artifacts are published on Bintray whenever a PR is merged on master.

New releases are
created by pushing a tag to this repository:

```
$ git tag -s v1.2.3
$ git push origin v1.2.3
```
