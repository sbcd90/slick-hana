name := "slick-hana"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
                          "com.typesafe.slick" %% "slick" % "3.2.0",
                          "org.scalatest" %% "scalatest" % "3.0.3",
                          "org.slf4j" % "slf4j-nop" % "1.7.25"
                        )
    