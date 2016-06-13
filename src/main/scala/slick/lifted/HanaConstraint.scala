package slick.lifted

import slick.ast.Node

object HanaIndexSortTypes extends Enumeration {
  type HanaIndexSortTypes = Value

  val asc, desc = Value
}

class HanaIndex(override val name: String, override val table: AbstractTable[_], override val on: IndexedSeq[Node], val sort: Seq[String], override val unique: Boolean)
  extends Index(name, table, on, unique)