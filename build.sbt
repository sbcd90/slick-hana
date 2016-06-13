name := "slick-hana"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
                          "com.typesafe.slick" %% "slick" % "3.2.0-SNAPSHOT",
                          "org.scalatest" %% "scalatest" % "2.2.6",
                          "org.slf4j" % "slf4j-nop" % "1.6.4"
                        )
    