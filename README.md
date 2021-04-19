## Modelling

![CI](https://github.com/sirgraystar/mandyville-modelling/actions/workflows/ci.yml/badge.svg)
[![codecov](https://codecov.io/gh/sirgraystar/mandyville-modelling/branch/main/graph/badge.svg?token=E901EPLINY)](https://codecov.io/gh/sirgraystar/mandyville-modelling)

Predictive modelling for mandyville. A reimplementation of my thesis
in Scala, but hopefully better and more accurate.

Uses data collected by
[mandyville-data](https://github.com/sirgraystar/mandyville-data)
and runs against the database schema produced by the migrations in
[mandyville-meta](https://github.com/sirgraystar/mandyville-meta).

### Goals
  * Extensible and flexbile player predictions
  * Predictions within a given time context
  * Model comparison and evaluation against actual results

### Requirements
  * `sbt`
  * Scala 2.13.0

### Testing

Just run `sbt test`. You can run `sbt coverageReport` to get a
coverage report if you so wish.
