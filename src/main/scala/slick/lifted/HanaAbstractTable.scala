package slick.lifted

object HanaAbstractTable {
  implicit class AbstractTableExt(table: AbstractTable[_]) {
    def hanaIndex[T](name: String, on: T, sort: Seq[HanaIndexSortTypes.Value] = null, unique: Boolean = false)(implicit shape: Shape[_ <: FlatShapeLevel, T, _, _]) =
      new HanaIndex(name, table, ForeignKey.linearizeFieldRefs(shape.toNode(on)), sort.map(s => s.toString), unique).asInstanceOf[Index]
  }
}