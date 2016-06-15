package slick.test

import slick.jdbc.HanaProfile.api._
import slick.lifted.{HanaIndexSortTypes, Tag}
import slick.lifted.HanaAbstractTable._
import slick.relational.{HanaTable, HanaTableTypes}
import slick.sql.SqlProfile.ColumnOption.Nullable

object HanaTest extends App {
  val db = Database.forConfig("hana")

  try {
    class Test1(tag: Tag) extends HanaTable[(Int, String)](tag, Some("I076326"), Some(HanaTableTypes.column), "TEST1") {

      def id = column[Int]("ID", O.PrimaryKey)
      def name = column[String]("NAME", O.Default("Subho"))

      def * = (id, name)
      def idx = this.hanaIndex("idx_a", (id), Seq(HanaIndexSortTypes.desc), true)
    }

    val test1 = TableQuery[Test1]

    class Test2(tag: Tag) extends HanaTable[(Int, String)](tag, Some("I076326"), Some(HanaTableTypes.column), "TEST2") {
      def supp_id = column[Int] ("SUPP_ID")
      def supp_name = column[String]("SUPP_NAME", Nullable)

      def * = (supp_id, supp_name)

      def test2 = foreignKey("SUP_FK", supp_id, test1)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

      def pk = primaryKey("pk_a", (supp_id))
    }

    val test2 = TableQuery[Test2]

    test1.schema.createStatements.foreach(println)
    test2.schema.createStatements.foreach(println)
    test2.schema.dropStatements.foreach(println)
    test1.schema.dropStatements.foreach(println)
    println(test1.filter(_.name === "Subho").result.statements.toList.head)
  } finally {
    db.close()
  }
}