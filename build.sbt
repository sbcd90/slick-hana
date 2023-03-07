name := "slick-hana"

version := "1.1-SNAPSHOT"

scalaVersion := "2.12.15"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
                          "com.typesafe.slick" %% "slick" % "3.4.1",
                          "com.sap.cloud.db.jdbc" % "ngdbc" % "2.15.12",
                          "org.scalatest" %% "scalatest" % "3.2.15" % "test",
                          "org.slf4j" % "slf4j-nop" % "2.0.6"
                        )