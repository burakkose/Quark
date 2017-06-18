package io.github.quark.operation

sealed trait OperationType

object OperationType {
  case object Incoming extends OperationType
  case object Routing extends OperationType
  case object Outgoing extends OperationType
}
