package io.github.quark.action

import shapeless._
import shapeless.ops.hlist.IsHCons

sealed trait ServiceAction

object ServiceAction {

  final case class Service[L <: HList](path: String)(ops: L)(
      implicit lUBConstraint: LUBConstraint[L, OperationAction],
      isDistinctConstraint: IsDistinctConstraint[L],
      isHCons: IsHCons[L])
      extends ServiceAction {
    def operation[T <: OperationAction](
        implicit operationSelector: OperationSelector[L, T]): T = {
      operationSelector(ops)
    }
  }

}
