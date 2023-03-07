package slick.jdbc

import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite
import slick.jdbc.HanaProfile.api._
import slick.jdbc.TestSuiteData._

class TableCUDBuilderTestSuite extends AnyFunSuite with BeforeAndAfter {

  before {
  }

  test("update table with where clause") {
    val update = coffees.filter(_.name === "Espresso").map(record => (record.name, record.supID, record.price, record.sales, record.total))
        .update("Espresso", 150, 9.99, 0, 0)

    assert(update.statements.size === 1)
    assert(update.statements.head === "update \"DUMMY_SCHEMA\".\"COFFEES\" set \"COF_NAME\" = ?, \"SUP_ID\" = ?, \"PRICE\" = ?, \"SALES\" = ?," +
      " \"TOTAL\" = ? where \"DUMMY_SCHEMA\".\"COFFEES\".\"COF_NAME\" = 'Espresso'")
  }

  test("upsert table with where clause") {
    val upsert = coffees.map(record => (record.name, record.supID, record.price, record.sales, record.total))
      .insertOrUpdate("Espresso", 150, 9.99, 0, 0)

    assert(upsert.statements.size === 1)
    assert(upsert.statements.head === "upsert \"DUMMY_SCHEMA\".\"COFFEES\"(\"COF_NAME\", \"SUP_ID\", \"PRICE\", \"SALES\", \"TOTAL\")" +
      " values(?, ?, ?, ?, ?) with primary key")
  }

  test("insert record into table") {
    val insert = coffees.insertStatement

    assert(insert === "insert into \"DUMMY_SCHEMA\".\"COFFEES\" (\"COF_NAME\",\"SUP_ID\",\"PRICE\",\"SALES\",\"TOTAL\")  values (?,?,?,?,?)")
  }

  test("delete record from table") {
    val delete = coffees.filter(_.name === "Espresso").delete

    assert(delete.statements.size === 1)
    assert(delete.statements.head === "delete from \"DUMMY_SCHEMA\".\"COFFEES\" where \"DUMMY_SCHEMA\".\"COFFEES\".\"COF_NAME\" = 'Espresso'")
  }
}