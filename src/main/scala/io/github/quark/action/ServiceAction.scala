package io.github.quark.action

import scala.reflect.ClassTag

sealed trait ServiceAction {
  def path: String

  def ops: Seq[OperationAction]

  def operation[T <: OperationAction](implicit tag: ClassTag[T]): Option[T]
}

object ServiceAction {

  final case class Service(path: String, ops: Seq[OperationAction])
      extends ServiceAction {
    def operation[T <: OperationAction](implicit tag: ClassTag[T]): Option[T] = {
      ops.flatMap {
        case operation: T => Some(operation)
        case _ => None
      }.headOption
    }
  }

}
