package slick.jdbc

import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.lifted.{HanaIndexSortTypes, TableQuery, Tag}
import slick.relational.HanaTable
import slick.lifted.HanaAbstractTable._
import slick.jdbc.HanaProfile.api._

class TableDDLBuilderTestSuite extends FunSuite with BeforeAndAfter {
  class Product(tag: Tag) extends HanaTable[(Int, String)](tag, Some("DUMMY_SCHEMA"), "PRODUCT") {
    def id = column[Int]("ID", O.PrimaryKey)
    def name = column[String]("NAME", O.SqlType("VARCHAR(20)"))

    def * = (id, name)
  }

  class Students(tag: Tag) extends HanaTable[(Int, Int, String)](tag, Some("DUMMY_SCHEMA"), "STUDENTS") {
    def standard = column[Int]("STD", O.PrimaryKey)
    def rollno = column[Int]("ROLLNO", O.PrimaryKey)
    def name = column[String]("NAME")

    def * = (standard, rollno, name)
  }

  class Suppliers(tag: Tag) extends HanaTable[(Int, String, String, String, String, String)](tag, Some("DUMMY_SCHEMA"), "SUPPLIERS") {
    def id = column[Int]("ID", O.PrimaryKey)
    def name = column[String]("SUP_NAME")
    def street = column[String]("STREET")
    def city = column[String]("CITY")
    def state = column[String]("STATE")
    def zip = column[String]("ZIP")

    def * = (id, name, street, city, state, zip)

    def idx1 = this.hanaIndex("Idx1", (id, name), Seq(HanaIndexSortTypes.desc, HanaIndexSortTypes.asc), true)
    def idx2 = this.hanaIndex("Idx2", (id, name))
  }

  class Coffees(tag: Tag) extends HanaTable[(String, Int, Double, Int, Int)](tag, Some("DUMMY_SCHEMA"), "COFFEES") {
    def name = column[String]("COF_NAME", O.PrimaryKey)
    def supID = column[Int]("SUP_ID")
    def price = column[Double]("PRICE")
    def sales = column[Int]("SALES")
    def total = column[Int]("TOTAL")

    def * = (name, supID, price, sales, total)
    def fk1 = foreignKey("fk1", supID, TableQuery[Suppliers])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
    def pk1 = primaryKey("pk1", (name))
  }

  class Employees(tag: Tag) extends HanaTable[(Int, String, String)](tag, Some("DUMMY_SCHEMA"), "EMPLOYEES") {
    def id = column[Int]("COMPANY_ID")
    def name = column[String]("COMPANY")
    def emp_name = column[String]("EMP_NAME")

    def * = (id, name, emp_name)
    def pk1 = primaryKey("pk1", (name))
    def pk2 = primaryKey("pk2", (id))
  }

  val product = TableQuery[Product]
  val students = TableQuery[Students]
  val suppliers = TableQuery[Suppliers]
  val coffees = TableQuery[Coffees]
  val employees = TableQuery[Employees]

  before {
  }

  test("create table statement") {
    val statements = product.schema.createStatements.toList
    assert(statements.size == 1, s"There is only ${statements.size} number of create statements generated")
    assert(statements.head == "create table \"DUMMY_SCHEMA\".\"PRODUCT\" (\"ID\" INTEGER PRIMARY KEY,\"NAME\" VARCHAR(20))")
  }

  // should throw SlickException
  test("create table statement with composite primary key") {
    try {
      val statements = students.schema.createStatements.toList
      assert(statements.size == 1, s"There is only ${statements.size} number of create statements generated")
      assert(statements.head == "create table \"DUMMY_SCHEMA\".\"STUDENTS\" (\"STD\" INTEGER PRIMARY KEY,\"ROLLNO\" INTEGER PRIMARY KEY,\"NAME\" VARCHAR(254))")
    } catch {
      case e: SlickException => assert(e.getMessage == "Table STUDENTS defines multiple primary key columns")
    }
  }

  test("create table with index") {
    val statements = suppliers.schema.createStatements.toList
    statements.foreach(stmt => {
      if (stmt.contains("Idx1")) {
        assert(stmt == "create unique index \"Idx1\" on \"DUMMY_SCHEMA\".\"SUPPLIERS\" (\"ID\" desc,\"SUP_NAME\" asc)")
      }
    })
  }

  test("create table with index without sort") {
    val statements = suppliers.schema.createStatements.toList
    statements.foreach(stmt => {
      if (stmt.contains("Idx2")) {
        assert(stmt == "create index \"Idx2\" on \"DUMMY_SCHEMA\".\"SUPPLIERS\" (\"ID\",\"SUP_NAME\")")
      }
    })
  }

  test("add foreign key constraint to a table") {
    val statements = coffees.schema.createStatements.toList
    statements.foreach(stmt => {
      if (stmt.contains("fk1")) {
        assert(stmt == "alter table \"DUMMY_SCHEMA\".\"COFFEES\" add constraint \"fk1\" foreign key(\"SUP_ID\") " +
          "references \"DUMMY_SCHEMA\".\"SUPPLIERS\"(\"ID\") on update RESTRICT on delete CASCADE")
      }
    })
  }

  test("add primary key constraint to a table") {
    val statements = coffees.schema.createStatements.toList
    statements.foreach(stmt => {
      if (stmt.contains("pk1")) {
        assert(stmt == "alter table \"DUMMY_SCHEMA\".\"COFFEES\" add constraint \"pk1\" primary key(\"COF_NAME\")")
      }
    })
  }

  test("failure on multiple primary key constraints") {
    try {
      employees.schema.createStatements.toList
    } catch {
      case e: SlickException => assert(e.getMessage == "Table EMPLOYEES defines multiple primary keys (pk1, pk2)")
    }
  }

  test("drop table") {
    val statements = product.schema.dropStatements.toList
    assert(statements.size == 1)
    assert(statements.head == "drop table \"DUMMY_SCHEMA\".\"PRODUCT\"")
  }
}