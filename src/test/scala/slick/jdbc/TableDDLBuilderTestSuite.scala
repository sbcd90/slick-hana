package slick.jdbc

import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.lifted.{TableQuery, Tag}
import slick.relational.HanaTable

import slick.jdbc.HanaProfile.api._

class TableDDLBuilderTestSuite extends FunSuite with BeforeAndAfter {
  class Product(tag: Tag) extends HanaTable[(Int, String)](tag, Some("DUMMY_SCHEMA"), "PRODUCT") {
    def id = column[Int]("ID", O.PrimaryKey)
    def name = column[String]("NAME")

    def * = (id, name)
  }

  val product = TableQuery[Product]

  before {
  }

  test("create table statement") {
    product.schema.createStatements.foreach(println)
  }
}