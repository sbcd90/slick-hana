package slick.relational

import slick.jdbc.HanaProfile.api._
import slick.lifted.Tag

object HanaTableTypes extends Enumeration {
  type HanaTableTypes = Value

  val column, row = Value
}

abstract class HanaTable[T](_tableTag: Tag, _schemaName: Option[String], _tableType: Option[HanaTableTypes.Value], _tableName: String)
  extends Table[T](_tableTag, _schemaName, _tableName) {

  def this(_tableTag: Tag, _schemaName: Option[String], _tableName: String) = this(_tableTag, _schemaName, None, _tableName)

  val tableType = if (_tableType.isDefined) _tableType.get.toString else null
}