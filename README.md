slick-hana
==========

`slick-hana` is an extension of `slick` for SAP Hana. 

[Slick](https://github.com/slick/slick) is a modern database query and access library for Scala. Slick provides a Functional Relational mapping for most modern databases.

## Build & Run

`slick-hana` requires the dependency to SAP Hana Jdbc Driver. The dependency can be added like this:

```
"com.sap.cloud.db.jdbc" % "ngdbc" % "2.15.12",
```

`slick-hana` is a scala module & can be compiled using

```
sbt compile
```

& can be run using

```
sbt run
```

To install into local repo

```
sbt publishLocal
```

Examples
========

[Hana Connect Example](https://github.com/akirafujiu/slick-hana/blob/main/src/main/scala/slick/example/HanaConnectExample.scala)