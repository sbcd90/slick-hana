package slick.jdbc

import slick.lifted.{HanaIndexSortTypes, TableQuery, Tag}
import slick.relational.{HanaTable, HanaTableTypes}
import slick.sql.SqlProfile.ColumnOption.{NotNull, Nullable}
import slick.jdbc.HanaProfile.api._
import slick.lifted.HanaAbstractTable._

object TestSuiteData {
  class Product(tag: Tag) extends HanaTable[(Int, String, String)](tag, Some("DUMMY_SCHEMA"), "PRODUCT") {
    def id = column[Int]("ID", O.PrimaryKey)
    def name = column[String]("NAME", O.SqlType("VARCHAR(20)"), Nullable)
    def isbnNo = column[String]("ISBN_NO", NotNull)

    def * = (id, name, isbnNo)
  }

  class Students(tag: Tag) extends HanaTable[(Int, Int, String)](tag, Some("DUMMY_SCHEMA"), "STUDENTS") {
    def standard = column[Int]("STD", O.PrimaryKey)
    def rollno = column[Int]("ROLLNO", O.PrimaryKey)
    def name = column[String]("NAME", Nullable)

    def * = (standard, rollno, name)
  }

  class Suppliers(tag: Tag) extends HanaTable[(Int, String, String, String, String, String)](tag, Some("DUMMY_SCHEMA"), "SUPPLIERS") {
    def id = column[Int]("ID", O.PrimaryKey)
    def name = column[String]("SUP_NAME", Nullable)
    def street = column[String]("STREET", Nullable)
    def city = column[String]("CITY", Nullable)
    def state = column[String]("STATE", Nullable)
    def zip = column[String]("ZIP", Nullable)

    def * = (id, name, street, city, state, zip)

    def idx1 = this.hanaIndex("Idx1", (id, name), Seq(HanaIndexSortTypes.desc, HanaIndexSortTypes.asc), true)
    def idx2 = this.hanaIndex("Idx2", (id, name))
  }

  class Coffees(tag: Tag) extends HanaTable[(String, Int, Double, Int, Int)](tag, Some("DUMMY_SCHEMA"), "COFFEES") {
    def name = column[String]("COF_NAME", O.PrimaryKey)
    def supID = column[Int]("SUP_ID", Nullable)
    def price = column[Double]("PRICE", Nullable)
    def sales = column[Int]("SALES", Nullable)
    def total = column[Int]("TOTAL", Nullable)

    def * = (name, supID, price, sales, total)
    def fk1 = foreignKey("fk1", supID, TableQuery[Suppliers])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
    def pk1 = primaryKey("pk1", (name))
  }

  class Employees(tag: Tag) extends HanaTable[(Int, String, String)](tag, Some("DUMMY_SCHEMA"), "EMPLOYEES") {
    def id = column[Int]("COMPANY_ID", O.PrimaryKey)
    def name = column[String]("COMPANY", Nullable)
    def emp_name = column[String]("EMP_NAME", Nullable)

    def * = (id, name, emp_name)
    def pk1 = primaryKey("pk1", (name))
    def pk2 = primaryKey("pk2", (id))
  }

  class Book_Content(tag: Tag) extends HanaTable[(Int, Int, String)](tag, Some("DUMMY_SCHEMA"), Some(HanaTableTypes.column), "BOOK_CONTENT") {
    def pageno = column[Int]("PAGENO", O.PrimaryKey, O.AutoInc)
    def lastbookmark = column[Int]("BOOKMARK", O.Default(0))
    def desc = column[String]("DESCRIPTION", O.SqlType("CHAR(100)"))

    def * = (pageno, lastbookmark, desc)
  }

  val product = TableQuery[Product]
  val students = TableQuery[Students]
  val suppliers = TableQuery[Suppliers]
  val coffees = TableQuery[Coffees]
  val employees = TableQuery[Employees]
  val book_Content = TableQuery[Book_Content]
}