package slick.jdbc

import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite
import slick.jdbc.HanaProfile.api._
import slick.jdbc.TestSuiteData._

class QueryBuilderTestSuite extends AnyFunSuite with BeforeAndAfter {
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

  test("select statement with orderby clause") {
    val statements = suppliers.sortBy(_.id.asc.nullsLast).sortBy(_.name.desc.nullsFirst).sortBy(_.city.asc.nullsDefault).result.statements.toList
    assert(statements.size === 1)
    assert(statements.head === "select \"ID\", \"SUP_NAME\", \"STREET\", \"CITY\", \"STATE\", \"ZIP\" from \"DUMMY_SCHEMA\".\"SUPPLIERS\" " +
      "order by \"ID\" nulls last, \"SUP_NAME\" desc nulls first, \"CITY\"")
  }

  test("select statement with or clause") {
    val criteriaColombian = Option("Colombian")
    val criteriaEspresso = Option("Espresso")
    val criteriaRoast: Option[String] = None

    val statements = coffees.filter(coffee =>
      List(criteriaColombian.map(coffee.name === _),
        criteriaEspresso.map(coffee.name === _),
        criteriaRoast.map(coffee.name === _))
        .collect({case Some(criteria)  => criteria}).reduceLeftOption(_ || _).get).result.statements.toList
    assert(statements.size === 1)
    assert(statements.head === "select \"COF_NAME\", \"SUP_ID\", \"PRICE\", \"SALES\", \"TOTAL\" from \"DUMMY_SCHEMA\".\"COFFEES\" " +
      "where (\"COF_NAME\" = 'Colombian') or (\"COF_NAME\" = 'Espresso')")
  }

  test("select statement with or & and clause") {
    val criteriaColombian = Option("Colombian")
    val criteriaEspresso = Option("Espresso")
    val criteriaRoast: Option[String] = None

    val statements = coffees.filter(coffee =>
      List(criteriaColombian.map(coffee.name === _),
        criteriaEspresso.map(coffee.name === _),
        criteriaRoast.map(coffee.name === _))
        .collect({case Some(criteria)  => criteria}).reduceLeftOption(_ || _).get)
        .filter(_.sales > 5000).result.statements.toList
    assert(statements.size === 1)
    assert(statements.head === "select \"COF_NAME\", \"SUP_ID\", \"PRICE\", \"SALES\", \"TOTAL\" from \"DUMMY_SCHEMA\".\"COFFEES\" " +
      "where ((\"COF_NAME\" = 'Colombian') or (\"COF_NAME\" = 'Espresso')) and (\"SALES\" > 5000)")
  }

  test("select with cross-joins or cartesian products") {
    val crossJoin = for {
      (c,s) <- coffees join suppliers
    } yield (c.name, s.name)

    val statements = crossJoin.result.statements.toList
    assert(statements.size === 1)
    assert(statements.head === "select x2.\"COF_NAME\", x3.\"SUP_NAME\" from \"DUMMY_SCHEMA\".\"COFFEES\" x2, " +
      "\"DUMMY_SCHEMA\".\"SUPPLIERS\" x3")
  }

  test("select with inner/outer joins") {
    val innerJoin = for {
      (c,s) <- coffees join suppliers on (_.supID === _.id)
    } yield (c.name, s.name)

    val leftOuterJoin = for {
      (c,s) <- coffees joinLeft suppliers on (_.supID === _.id)
    } yield (c.name, s.map(_.name))

    val rightOuterJoin = for {
      (c,s) <- coffees joinRight suppliers on (_.supID === _.id)
    } yield (c.map(_.name), s.name)

    val fullOuterJoin = for {
      (c,s) <- coffees joinFull suppliers on (_.supID === _.id)
    } yield (c.map(_.name), s.map(_.name))

    val stmtInnerJoin = innerJoin.result.statements.toList
    assert(stmtInnerJoin.size === 1)
    assert(stmtInnerJoin.head === "select x2.\"COF_NAME\", x3.\"SUP_NAME\" from \"DUMMY_SCHEMA\".\"COFFEES\" x2, " +
      "\"DUMMY_SCHEMA\".\"SUPPLIERS\" x3 where x2.\"SUP_ID\" = x3.\"ID\"")

    val stmtLeftOuterJoin = leftOuterJoin.result.statements.toList
    assert(stmtLeftOuterJoin.size === 1)
    assert(stmtLeftOuterJoin.head === "select x2.\"COF_NAME\", (case when (x3.\"ID\" is not null) then x3.\"SUP_NAME\"" +
      " else null end) from \"DUMMY_SCHEMA\".\"COFFEES\" x2 left outer join \"DUMMY_SCHEMA\".\"SUPPLIERS\" x3 on x2.\"SUP_ID\" = x3.\"ID\"")

    val stmtRightOuterJoin = rightOuterJoin.result.statements.toList
    assert(stmtRightOuterJoin.size === 1)
    assert(stmtRightOuterJoin.head === "select (case when (x2.\"COF_NAME\" is not null) then x2.\"COF_NAME\" else null end), x3.\"SUP_NAME\"" +
      " from \"DUMMY_SCHEMA\".\"COFFEES\" x2 right outer join \"DUMMY_SCHEMA\".\"SUPPLIERS\" x3 on x2.\"SUP_ID\" = x3.\"ID\"")

    val stmtFullOuterJoin = fullOuterJoin.result.statements.toList
    assert(stmtFullOuterJoin.size === 1)
    assert(stmtFullOuterJoin.head === "select (case when (x2.\"COF_NAME\" is not null) then x2.\"COF_NAME\" else null end), (case when (x3.\"ID\"" +
      " is not null) then x3.\"SUP_NAME\" else null end) from \"DUMMY_SCHEMA\".\"COFFEES\" x2 full outer join \"DUMMY_SCHEMA\".\"SUPPLIERS\" x3 on x2.\"SUP_ID\" = x3.\"ID\"")
  }

  test("select with monadic joins") {
    val monadicJoin = for {
      c <- coffees
      s <- suppliers
    } yield(c.name, s.name)

    val stmtMonadicJoin = monadicJoin.result.statements.toList
    assert(stmtMonadicJoin.size === 1)
    assert(stmtMonadicJoin.head === "select x2.\"COF_NAME\", x3.\"SUP_NAME\" from \"DUMMY_SCHEMA\".\"COFFEES\" x2," +
      " \"DUMMY_SCHEMA\".\"SUPPLIERS\" x3")
  }

  test("select with union/union all") {
    val q1 = coffees.filter(_.price < 8.0)
    val q2 = coffees.filter(_.price > 9.0)

    val union = q1 union q2
    val unionStmt = union.result.statements.toList
    assert(unionStmt.size === 1)
    assert(unionStmt.head === "(select \"COF_NAME\" as x2, \"SUP_ID\" as x3, \"PRICE\" as x4, \"SALES\" as x5, \"TOTAL\" as x6 from" +
      " \"DUMMY_SCHEMA\".\"COFFEES\" where \"PRICE\" < 8.0) union (select \"COF_NAME\" as x2, \"SUP_ID\" as x3, \"PRICE\" as x4, \"SALES\" as x5," +
      " \"TOTAL\" as x6 from \"DUMMY_SCHEMA\".\"COFFEES\" where \"PRICE\" > 9.0)")

    val unionAll = q1 ++ q2
    val unionAllStmt = unionAll.result.statements.toList
    assert(unionAllStmt.size === 1)
    assert(unionAllStmt.head === "(select \"COF_NAME\" as x2, \"SUP_ID\" as x3, \"PRICE\" as x4, \"SALES\" as x5, \"TOTAL\" as x6 from \"DUMMY_SCHEMA\".\"COFFEES\"" +
      " where \"PRICE\" < 8.0) union all (select \"COF_NAME\" as x2, \"SUP_ID\" as x3, \"PRICE\" as x4, \"SALES\" as x5, \"TOTAL\" as x6 from \"DUMMY_SCHEMA\".\"COFFEES\"" +
      " where \"PRICE\" > 9.0)")
  }

  test("select with aggregates") {
    val q = (for {
      c <- coffees
    } yield (c.name, c.price)).groupBy(_._1)

    val q1 = q.map { case (name, price) =>
      (name, price.map(_._2).min)
    }
    val q1stmt = q1.result.statements.toList
    assert(q1stmt.size === 1)
    assert(q1stmt.head === "select \"COF_NAME\", min(\"PRICE\") from \"DUMMY_SCHEMA\".\"COFFEES\" group by \"COF_NAME\"")

    val qnew = coffees.map(_.price)
    val q2 = qnew.max
    val q2stmt = q2.result.statements.toList
    assert(q2stmt.size === 1)
    assert(q2stmt.head === "select max(\"PRICE\") from \"DUMMY_SCHEMA\".\"COFFEES\"")

    val q3 = qnew.sum
    val q3stmt = q3.result.statements.toList
    assert(q3stmt.size === 1)
    assert(q3stmt.head === "select sum(\"PRICE\") from \"DUMMY_SCHEMA\".\"COFFEES\"")

    val q4 = qnew.avg
    val q4stmt = q4.result.statements.toList
    assert(q4stmt.size === 1)
    assert(q4stmt.head === "select avg(\"PRICE\") from \"DUMMY_SCHEMA\".\"COFFEES\"")
  }
}