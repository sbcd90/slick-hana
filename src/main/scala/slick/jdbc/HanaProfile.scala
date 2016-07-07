package slick.jdbc

import com.typesafe.config.Config
import slick.SlickException
import slick.ast.{FieldSymbol, Node, Select, TableNode}
import slick.basic.Capability
import slick.compiler.CompilerState
import slick.lifted.{HanaIndex, Index, PrimaryKey}
import slick.util.MacroSupport._
import slick.relational.{HanaTable, HanaTableTypes}
import slick.util.ConstArray

trait HanaProfile extends JdbcProfile { profile =>

  override protected def computeCapabilities: Set[Capability] = (super.computeCapabilities
    - JdbcCapabilities.insertOrUpdate
    - JdbcCapabilities.nullableNoDefault)

  override protected[this] def loadProfileConfig: Config = {
    super.loadProfileConfig
  }

  // used by TableDDLBuilder and ColumnDDLBuilder
  var primaryKeyColumns = 0

  override def createQueryBuilder(n: Node, state: CompilerState): QueryBuilder = new QueryBuilder(n, state)
  override def createTableDDLBuilder(table: Table[_]): TableDDLBuilder = new TableDDLBuilder(table)
  override def createColumnDDLBuilder(column: FieldSymbol, table: Table[_]): ColumnDDLBuilder = new ColumnDDLBuilder(column)

  class TableDDLBuilder(table: Table[_]) extends super.TableDDLBuilder(table) {
    private val sapTable: HanaTable[_] = try {
      table.asInstanceOf[HanaTable[_]]
    } catch {
      case e: Exception => throw new SlickException("The table object is not of type HanaTable")
    }

    override protected def createTable: String = {
      var b = new StringBuilder append ""
      if (sapTable.tableType != null) {
        b = b append s"create ${sapTable.tableType} table " append quoteTableName(tableNode) append " ("
      } else {
        b = b append s"create table " append quoteTableName(tableNode) append " ("
      }
      var first = true
      primaryKeyColumns = 0
      for (c <- columns) {
        if (first) first = false else b append ","
        c.asInstanceOf[ColumnDDLBuilder].appendHanaColumn(b, sapTable.tableType)
      }

      if (primaryKeyColumns > 1) {
        throw new SlickException("Table "+tableNode.tableName+" defines multiple primary key columns")
      }

      addTableOptions(b)
      b.append(")")
      b.toString()
    }

    override protected def createIndex(idx: Index): String = {
      val sapIdx = try {
        idx.asInstanceOf[HanaIndex]
      } catch {
        case e: Exception => throw new SlickException("The index object is not of type HanaIndex")
      }

      val b = new StringBuilder append "create "
      if (sapIdx.unique) b append "unique "
      b append "index " append quoteIdentifier(sapIdx.name) append " on " append quoteTableName(tableNode) append " ("
      addIndexToColumnList(sapIdx.on, b, sapIdx.table.tableName, sapIdx.sort)
      b.append(")")
      b.toString()
    }

    override protected def createPrimaryKey(pk: PrimaryKey): String = {
      if (pk.columns.size > 1)
        throw new SlickException("Table "+tableNode.tableName+" defines multiple primary key columns in "
          + pk.name)

      val sb = new StringBuilder append "alter table " append quoteTableName(tableNode) append " add "
      addPrimaryKey(pk, sb)
      sb.toString()
    }

    def addIndexToColumnList(columns: IndexedSeq[Node], sb: StringBuilder, requiredTableName: String, sort: Seq[String]) = {
      var first = true
      var count = 0
      for(c <- columns) c match {
        case Select(t: TableNode, field: FieldSymbol) =>
          if(first) first = false
          else sb append ","

          if (sort != null) {
            sb append quoteIdentifier(field.name) append " " append sort(count)
          } else {
            sb append quoteIdentifier(field.name)
          }
          if(requiredTableName != t.tableName)
            throw new SlickException("All columns in index must belong to table "+requiredTableName)
          count += 1
        case _ => throw new SlickException("Cannot use column "+c+" in index (only named columns are allowed)")
      }
    }
  }

  class ColumnDDLBuilder(column: FieldSymbol) extends super.ColumnDDLBuilder(column) {
    if (primaryKey) {
      primaryKeyColumns += 1
    }

    def appendHanaColumn(sb: StringBuilder, tableType: String) = {
      sb append quoteIdentifier(column.name) append ' '
      appendType(sb)
      appendOptions(sb, tableType)
    }

    protected def appendOptions(sb: StringBuilder, tableType: String) = {
      if (defaultLiteral ne null) sb append " DEFAULT " append defaultLiteral
      if (autoIncrement) {
        if (tableType != null &&  tableType == HanaTableTypes.column.toString) {
          sb append " GENERATED BY DEFAULT AS IDENTITY(START WITH 1)"
        } else {
          throw new SlickException("Auto Increment not possible if table is not Column Table")
        }
      }
      if (notNull) sb append " NOT NULL"
      if (primaryKey) {
        primaryKeyColumns += 1
        sb append " PRIMARY KEY"
      }
    }
  }

  class QueryBuilder(tree: Node, state: CompilerState) extends super.QueryBuilder(tree, state) {
    override protected def buildFetchOffsetClause(fetch: Option[Node], offset: Option[Node]) = (fetch, offset) match {
      case (Some(t), Some(d)) => b"\nlimit $t offset $d"
      case (Some(t), None) => b"\nlimit $t"
      case (None, Some(d)) => throw new SlickException("Offset clause without Limit is not supported by HANA")
      case _ => // nothing to do
    }

    override protected def buildOrderByClause(order: ConstArray[(Node, slick.ast.Ordering)]) = building(OtherPart) {
      if (!order.isEmpty) {
        b"\norder by "
        val newOrder = order.toArray.reverse
        b.sep(newOrder, ", "){ case (n, o) => buildOrdering(n, o)}
      }
    }
  }
}

object HanaProfile extends HanaProfile {

}