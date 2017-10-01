package io.github.quark.action

import shapeless.{TypeCase, Typeable}

sealed trait ServiceAction {
  def id: String

  def ops: Seq[OperationAction]

  def operation[T <: OperationAction: Typeable]: Option[T]
}

object ServiceAction {

  final case class Service(id: String, ops: Seq[OperationAction])
      extends ServiceAction {
    def operation[T <: OperationAction: Typeable]: Option[T] = {
      val actionType = TypeCase[T]
      ops.flatMap {
        case actionType(action) => Some(action)
        case _ => None
      }.headOption
    }
  }

}
