package slick.hana.example

import slick.jdbc.HanaProfile.api._
import slick.jdbc.meta.MTable
import slick.relational.HanaTable

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object HanaConnectExample extends App {
  val db = Database.forConfig("hana")

  try {
    class Students(tag: Tag) extends HanaTable[(Int, String)](tag, Some("TEST_CASE"), "STUDENTS") {
      def id = column[Int]("ID", O.PrimaryKey)
      def fullname = column[String]("NAME")

      def * = (id, fullname)
    }

    val students = TableQuery[Students]

    println(students.schema.createStatements.toList.head)

    val tableChecker: Future[Vector[MTable]] = db.run(MTable.getTables("%STUDENTS%"))
    val setupAction = DBIO.seq(
      students.schema.create,

      students ++= Seq(
        (1, "Ram"),
        (2, "Shyam")
      )
    )

    val setupFuture = tableChecker.flatMap(result =>
      db.run(setupAction)
    )

    val update = students.filter(_.id === 1).map(record => (record.id, record.fullname)).update(1, "Jadu")
    println(update.statements.toList.head)
    val updateFuture = setupFuture.flatMap(_ =>
      db.run(update)
    )

    println(students.result.statements.head)
    val f = updateFuture.flatMap(_ =>
      db.run(students.result).map(println)
    )

    Await.result(f, Duration.Inf)
  } finally {
    db.close()
  }
}