package slick.jdbc

import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.jdbc.HanaProfile.api._
import slick.jdbc.TestSuiteData._

class QueryBuilderTestSuite extends FunSuite with BeforeAndAfter {
  before {

  }

  test("normal select statement") {
    val statements = product.result.statements.toList
    assert(statements.size === 1)
    assert(statements.head === "select \"ID\", \"NAME\", \"ISBN_NO\" from \"DUMMY_SCHEMA\".\"PRODUCT\"")
  }

  test("select statement with a simple filter") {
    val statements = product.filter(_.name === "ERP").filter(_.id === 1).result.statements.toList
    assert(statements.size === 1)
    assert(statements.head === "select \"ID\", \"NAME\", \"ISBN_NO\" from \"DUMMY_SCHEMA\".\"PRODUCT\" " +
      "where (\"NAME\" = 'ERP') and (\"ID\" = 1)")
  }

  test("select statement with limit & offset") {
    val statements = suppliers.drop(1).take(2).result.statements.toList
    assert(statements.size === 1)
    assert(statements.head === "select \"ID\", \"SUP_NAME\", \"STREET\", \"CITY\", \"STATE\", \"ZIP\" " +
      "from \"DUMMY_SCHEMA\".\"SUPPLIERS\" limit 2 offset 1")
  }
}