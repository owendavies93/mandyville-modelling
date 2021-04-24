## Modelling

![CI](https://github.com/sirgraystar/mandyville-modelling/actions/workflows/ci.yml/badge.svg)
[![codecov](https://codecov.io/gh/sirgraystar/mandyville-modelling/branch/main/graph/badge.svg?token=E901EPLINY)](https://codecov.io/gh/sirgraystar/mandyville-modelling)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

Predictive modelling for mandyville. A reimplementation of my thesis
in Scala, but hopefully better and more accurate.

Uses data collected by
[mandyville-data](https://github.com/sirgraystar/mandyville-data)
and runs against the database schema produced by the migrations in
[mandyville-meta](https://github.com/sirgraystar/mandyville-meta).

### Goals
  * Extensible and flexible player predictions
  * Predictions within a given time context
  * Model comparison and evaluation against actual results

### Requirements
  * [`sbt`](https://github.com/sbt/sbt)
  * Scala 2.13.0 - the code is written with newer versions in mind, but some
    dependencies are still stuck on this version. scala-steward is set up
    to monitor this
  * docker, for integration testing
  * PostgreSQL 13.2 - older versions will probably work fine as well, but this
    is the version we use

### Testing

#### Unit Tests

Just run `sbt test`. You can run `sbt coverageReport` to get a
coverage report if you so wish.

#### Integration Tests

We use [`docker-it-scala`](https://github.com/whisklabs/docker-it-scala) to
run integration tests against a local PostgreSQL database. You can run these
tests with `sbt it:test`. The configuration should work pretty much out of the
box - you can make changes to the development database configuration in
[development.conf](/src/main/resources/development.conf). The database is
populated by [Flyway](https://github.com/flyway/flyway/) using the migrations
from [`meta`](https://github.com/sirgraystar/mandyville-meta).

You can run all tests, with coverage, and generating a report, with the
following command:

```bash
sbt clean coverage test it:test coverageReport
```
